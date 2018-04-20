/**
 * Copyright 2018 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.specs.clang.streamparserv2;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.suikasoft.jOptions.JOptionsUtils;
import org.suikasoft.jOptions.Datakey.DataKey;
import org.suikasoft.jOptions.Datakey.KeyFactory;
import org.suikasoft.jOptions.Interfaces.DataStore;
import org.suikasoft.jOptions.streamparser.LineStreamParserV2;

import com.google.common.base.Preconditions;

import pt.up.fe.specs.clang.ClangAstFileResource;
import pt.up.fe.specs.clang.ClangAstKeys;
import pt.up.fe.specs.clang.ClangAstParser;
import pt.up.fe.specs.clang.ClangAstResource;
import pt.up.fe.specs.clang.SupportedPlatform;
import pt.up.fe.specs.clang.datastore.LocalOptionsKeys;
import pt.up.fe.specs.clang.parsers.ClangParserKeys;
import pt.up.fe.specs.clang.parsers.ClangStreamParserV2;
import pt.up.fe.specs.clang.utils.ZipResourceManager;
import pt.up.fe.specs.clava.ClavaOptions;
import pt.up.fe.specs.clava.ast.extra.App;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;
import pt.up.fe.specs.util.parsing.arguments.ArgumentsParser;
import pt.up.fe.specs.util.providers.FileResourceManager;
import pt.up.fe.specs.util.providers.FileResourceProvider;
import pt.up.fe.specs.util.providers.FileResourceProvider.ResourceWriteData;
import pt.up.fe.specs.util.providers.ResourceProvider;
import pt.up.fe.specs.util.system.ProcessOutput;
import pt.up.fe.specs.util.utilities.LineStream;
import pt.up.fe.specs.util.utilities.StringLines;

public class ClangDumperParser {

    public final static DataKey<String> LINES_NOT_PARSED = KeyFactory.string("clang_dumper_parser_warnings");

    // private final static String CLANG_DUMP_FILENAME = "clangDump.txt";
    private final static String STDERR_DUMP_FILENAME = "stderr.txt";
    private static final String CLANGAST_RESOURCES_FILENAME = "clang_ast.resources";
    private final static String LOCAL_OPTIONS_FILE = "local_options.xml";

    // private final boolean dumpStdout;
    private final boolean useCustomResources;
    private final FileResourceManager clangAstResources;

    public ClangDumperParser() {
        this(false, false);

    }

    public ClangDumperParser(boolean dumpStdout, boolean useCustomResources) {
        // this.dumpStdout = dumpStdout;
        this.useCustomResources = useCustomResources;

        clangAstResources = FileResourceManager.fromEnum(ClangAstFileResource.class);

        if (this.useCustomResources) {
            clangAstResources.addLocalResources(CLANGAST_RESOURCES_FILENAME);
        }

    }

    public App parse(Collection<String> files, List<String> options) {

        return parse(files, ClavaOptions.toDataStore(options));
    }

    public App parse(Collection<String> files, DataStore config) {

        DataStore localData = JOptionsUtils.loadDataStore(LOCAL_OPTIONS_FILE, getClass(),
                LocalOptionsKeys.getProvider().getStoreDefinition());

        // Get version for the executable
        String version = config.get(ClangAstKeys.CLANGAST_VERSION);

        // Copy resources
        File clangExecutable = prepareResources(version);

        List<String> arguments = new ArrayList<>();
        arguments.add(clangExecutable.getAbsolutePath());

        arguments.addAll(files);

        arguments.add("--");

        // Add standard
        arguments.add(config.get(ClavaOptions.STANDARD).getFlag());

        List<String> systemIncludes = new ArrayList<>();

        // Add includes bundled with program
        // (only on Windows, it is expected that a Linux system has its own headers for libc/libc++)
        // if (Platforms.isWindows()) {
        systemIncludes.addAll(prepareIncludes(clangExecutable));
        // }

        // Add custom includes
        systemIncludes.addAll(localData.get(LocalOptionsKeys.SYSTEM_INCLUDES).getStringList());

        // Add local system includes
        // for (String systemInclude : localData.get(LocalOptionsKeys.SYSTEM_INCLUDES)) {
        for (String systemInclude : systemIncludes) {
            arguments.add("-isystem");
            arguments.add(systemInclude);
        }

        // If there still are arguments left using, pass them after '--'
        arguments.addAll(ArgumentsParser.newCommandLine().parse(config.get(ClavaOptions.FLAGS)));

        SpecsLogs.msgInfo("Calling Clang AST Dumper: " + arguments.stream().collect(Collectors.joining(" ")));

        // ProcessOutputAsString output = SpecsSystem.runProcess(arguments, true, false);
        // LineStreamParserV2 lineStreamParser = ClangStreamParserV2.newInstance();
        // if (SpecsSystem.isDebug()) {
        // lineStreamParser.getData().set(ClangParserKeys.DEBUG, true);
        // }

        ProcessOutput<String, DataStore> output = SpecsSystem.runProcess(arguments, this::processOutput,
                this::processStdErr);

        String warnings = output.getStdErr().get(LINES_NOT_PARSED);

        // Throw exception if there as any error
        if (output.getReturnValue() != 0) {
            throw new RuntimeException("There where errors during dumping:\n" + warnings);
        }

        // Check if there are error messages
        if (!warnings.isEmpty()) {
            SpecsLogs.msgInfo("There where warnings during dumping:\n" + warnings);
        }

        // Check if there where no interleaved executions
        checkInterleavedExecutions();

        ClangStreamParser clangStreamParser = new ClangStreamParser(output.getStdErr(), SpecsSystem.isDebug());
        return clangStreamParser.parse();
    }

    private DataStore processStdErr(InputStream inputStream) {
        // Create LineStreamParser
        LineStreamParserV2 lineStreamParser = ClangStreamParserV2.newInstance();

        // Set debug
        if (SpecsSystem.isDebug()) {
            lineStreamParser.getData().set(ClangParserKeys.DEBUG, true);
        }

        // Dump file
        File dumpfile = SpecsSystem.isDebug() ? new File(STDERR_DUMP_FILENAME) : null;

        // Parse input stream
        String linesNotParsed = lineStreamParser.parse(inputStream, dumpfile);

        // Add lines not parsed to DataStore
        DataStore data = lineStreamParser.getData();
        data.add(LINES_NOT_PARSED, linesNotParsed);

        // Return data
        return data;

    }

    private String processOutput(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        try (LineStream lines = LineStream.newInstance(inputStream, null)) {
            while (lines.hasNextLine()) {
                output.append(lines.nextLine()).append("\n");
            }
        }

        return output.toString();
    }

    /**
     *
     * @return path to the executable that was copied
     */
    private File prepareResources(String version) {

        File resourceFolder = getClangResourceFolder();

        SupportedPlatform platform = SupportedPlatform.getCurrentPlatform();
        FileResourceProvider executableResource = getExecutableResource(platform);

        // If version not defined, use the latest version of the resource
        if (version.isEmpty()) {
            version = executableResource.getVersion();
        }

        // ClangAst executable versions are separated by an underscore
        executableResource = executableResource.createResourceVersion("_" + version);

        // Copy executable
        ResourceWriteData executable = executableResource.writeVersioned(resourceFolder, ClangAstParser.class);

        // If Windows, copy additional dependencies
        if (platform == SupportedPlatform.WINDOWS) {
            for (FileResourceProvider resource : getWindowsResources()) {
                resource.writeVersioned(resourceFolder, ClangAstParser.class);
            }
        }

        // If file is new and we are in a flavor of Linux, make file executable
        if (executable.isNewFile() && platform.isLinux()) {
            SpecsSystem.runProcess(Arrays.asList("chmod", "+x", executable.getFile().getAbsolutePath()), false, true);
        }

        return executable.getFile();

    }

    public static File getClangResourceFolder() {
        String tempDir = System.getProperty("java.io.tmpdir");
        // String baseFilename = new JarPath(ClangAstLauncher.class, "clangjar").buildJarPath();
        // File resourceFolder = new File(baseFilename, "clang_ast_exe");
        File resourceFolder = new File(tempDir, "clang_ast_exe");
        return resourceFolder;
    }

    private FileResourceProvider getExecutableResource(SupportedPlatform platform) {

        switch (platform) {
        case WINDOWS:
            return clangAstResources.get(ClangAstFileResource.WIN_EXE);
        case CENTOS6:
            return clangAstResources.get(ClangAstFileResource.CENTOS6_EXE);
        case LINUX:
            return clangAstResources.get(ClangAstFileResource.LINUX_EXE);
        case MAC_OS:
            return clangAstResources.get(ClangAstFileResource.MAC_OS_EXE);
        default:
            throw new RuntimeException("Case not defined: '" + platform + "'");
        }
    }

    private List<FileResourceProvider> getWindowsResources() {
        List<FileResourceProvider> windowsResources = new ArrayList<>();

        windowsResources.add(clangAstResources.get(ClangAstFileResource.WIN_DLL1));
        windowsResources.add(clangAstResources.get(ClangAstFileResource.WIN_DLL2));
        windowsResources.add(clangAstResources.get(ClangAstFileResource.WIN_DLL3));

        return windowsResources;
    }

    private List<String> prepareIncludes(File clangExecutable) {

        File resourceFolder = getClangResourceFolder();

        File includesBaseFolder = SpecsIo.mkdir(resourceFolder, "clang_includes");
        ZipResourceManager zipManager = new ZipResourceManager(includesBaseFolder);

        // Clang built-in includes, to be used in all platforms
        // Write Clang headers
        ResourceWriteData builtinIncludesZip = clangAstResources.get(ClangAstFileResource.BUILTIN_INCLUDES_3_8)
                .writeVersioned(resourceFolder, ClangAstParser.class);
        // ResourceWriteData builtinIncludesZip = ClangAstWebResource.BUILTIN_INCLUDES_3_8.writeVersioned(
        // resourceFolder, ClangAstParser.class);

        // boolean hasFolderBeenCleared = false;

        zipManager.extract(builtinIncludesZip);

        // Test if include files are available
        boolean hasLibC = hasLibC(clangExecutable);

        if (!hasLibC) {
            // Obtain correct version of libc/c++
            FileResourceProvider libcResource = getLibCResource(SupportedPlatform.getCurrentPlatform());

            // Write Clang headers
            ResourceWriteData libcZip = libcResource.writeVersioned(resourceFolder,
                    ClangAstParser.class);

            zipManager.extract(libcZip);

        }

        // Add all folders inside base folder as system include
        List<String> includes = SpecsIo.getFolders(includesBaseFolder).stream()
                .map(file -> file.getAbsolutePath())
                .collect(Collectors.toList());

        // Sort them alphabetically, include order can be important
        Collections.sort(includes);

        return includes;
    }

    /**
     * Detects if the system has libc/licxx installed.
     * 
     * @param clangExecutable
     * @return
     */
    private boolean hasLibC(File clangExecutable) {

        File clangTest = SpecsIo.mkdir(SpecsIo.getTempFolder(), "clang_ast_test");

        boolean needsLib = Arrays.asList(ClangAstResource.TEST_INCLUDES_C, ClangAstResource.TEST_INCLUDES_CPP)
                .parallelStream()
                .map(resource -> testFile(clangExecutable, clangTest, resource))
                // Check if test fails in any of cases
                .filter(hasInclude -> !hasInclude)
                .findAny()
                .isPresent();

        if (needsLib) {
            SpecsLogs.msgLib("Could not find libc/licxx installed in the system");
        } else {
            SpecsLogs.msgLib("Detected libc and licxx installed in the system");
        }

        return !needsLib;
    }

    private FileResourceProvider getLibCResource(SupportedPlatform platform) {

        switch (platform) {
        case WINDOWS:
            return clangAstResources.get(ClangAstFileResource.LIBC_CXX_WINDOWS);
        // return ClangAstWebResource.LIBC_CXX_WINDOWS;
        case MAC_OS:
            return clangAstResources.get(ClangAstFileResource.LIBC_CXX_MAC_OS);
        // return ClangAstWebResource.LIBC_CXX_MAC_OS;
        default:
            throw new RuntimeException("LibC/C++ not available for platform '" + platform + "'");
        }
    }

    private boolean testFile(File clangExecutable, File testFolder, ResourceProvider testResource) {
        File testFile = testResource.write(testFolder);

        List<String> arguments = Arrays.asList(clangExecutable.getAbsolutePath(), testFile.getAbsolutePath(), "--");

        // LineStreamParserV2 clangStreamParser = ClangStreamParserV2.newInstance();
        ProcessOutput<String, DataStore> output = SpecsSystem.runProcess(arguments,
                this::processOutput, this::processStdErr);

        boolean foundInclude = !output.getStdOut().isEmpty();

        return foundInclude;
    }

    private static void checkInterleavedExecutions() {
        File consumerOrder = new File("consumer_order.txt");
        if (!consumerOrder.isFile()) {
            SpecsLogs.msgInfo("Could not find file 'consumer_order.txt'");
            return;
        }

        List<String> lines = StringLines.getLines(new File("consumer_order.txt"));

        // if (lines.size() % 2 == 0) {
        // LoggingUtils.msgWarn("Expected even number of lines, got '" + lines.size() + "'");
        // return;
        // }
        Preconditions.checkArgument(lines.size() % 2 == 0, "Expected even number of lines, got '" + lines.size() + "'");

        String line1Prefix = "ASTConsumer built ";
        String line2Prefix = "ASTConsumer destroyed ";

        for (int i = 0; i < lines.size(); i += 2) {
            String line1 = lines.get(i);
            String line2 = lines.get(i + 1);

            Preconditions.checkArgument(line1.startsWith(line1Prefix));
            Preconditions.checkArgument(line2.startsWith(line2Prefix));

            String subString1 = line1.substring(line1Prefix.length());
            String subString2 = line2.substring(line2Prefix.length());

            Preconditions.checkArgument(subString1.equals(subString2));
        }

    }

}