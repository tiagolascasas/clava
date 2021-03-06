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

package pt.up.fe.specs.clava.ast.attr.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import pt.up.fe.specs.util.SpecsStrings;
import pt.up.fe.specs.util.enums.EnumHelperWithValue;
import pt.up.fe.specs.util.lazy.Lazy;
import pt.up.fe.specs.util.providers.StringProvider;
import pt.up.fe.specs.util.utilities.CachedItems;

public enum AttributeKind implements StringProvider {

    FallThrough,
    Suppress,
    SwiftContext,
    SwiftErrorResult,
    SwiftIndirectResult,
    Annotate,
    CFConsumed,
    CarriesDependency,
    NSConsumed,
    NonNull,
    PassObjectSize,
    AMDGPUFlatWorkGroupSize,
    AMDGPUNumSGPR,
    AMDGPUNumVGPR,
    AMDGPUWavesPerEU,
    ARMInterrupt,
    AVRInterrupt,
    AVRSignal,
    AcquireCapability,
    AcquiredAfter,
    AcquiredBefore,
    AlignMac68k,
    Aligned,
    AllocAlign,
    AllocSize,
    AlwaysInline,
    AnalyzerNoReturn,
    AnyX86Interrupt,
    AnyX86NoCallerSavedRegisters,
    AnyX86NoCfCheck,
    ArcWeakrefUnavailable,
    ArgumentWithTypeTag,
    Artificial,
    AsmLabel,
    AssertCapability,
    AssertExclusiveLock,
    AssertSharedLock,
    AssumeAligned,
    Availability,
    Blocks,
    C11NoReturn,
    CDecl,
    CFAuditedTransfer,
    CFReturnsNotRetained,
    CFReturnsRetained,
    CFUnknownTransfer,
    CPUDispatch,
    CPUSpecific,
    CUDAConstant,
    CUDADevice,
    CUDAGlobal,
    CUDAHost,
    CUDAInvalidTarget,
    CUDALaunchBounds,
    CUDAShared,
    CXX11NoReturn,
    CallableWhen,
    Capability,
    CapturedRecord,
    Cleanup,
    CodeSeg,
    Cold,
    Common,
    Const,
    Constructor,
    Consumable,
    ConsumableAutoCast,
    ConsumableSetOnRead,
    Convergent,
    DLLExport,
    DLLImport,
    Deprecated,
    Destructor,
    DiagnoseIf,
    DisableTailCalls,
    EmptyBases,
    EnableIf,
    EnumExtensibility,
    ExclusiveTrylockFunction,
    ExternalSourceSymbol,
    FastCall,
    Final,
    FlagEnum,
    Flatten,
    Format,
    FormatArg,
    GNUInline,
    GuardedBy,
    GuardedVar,
    Hot,
    IBAction,
    IBOutlet,
    IBOutletCollection,
    InitPriority,
    IntelOclBicc,
    InternalLinkage,
    LTOVisibilityPublic,
    LayoutVersion,
    LifetimeBound,
    LockReturned,
    LocksExcluded,
    MSABI,
    MSInheritance,
    MSNoVTable,
    MSP430Interrupt,
    MSStruct,
    MSVtorDisp,
    MaxFieldAlignment,
    MayAlias,
    MicroMips,
    MinSize,
    MinVectorWidth,
    Mips16,
    MipsInterrupt,
    MipsLongCall,
    MipsShortCall,
    NSConsumesSelf,
    NSReturnsAutoreleased,
    NSReturnsNotRetained,
    NSReturnsRetained,
    Naked,
    NoAlias,
    NoCommon,
    NoDebug,
    NoDuplicate,
    NoInline,
    NoInstrumentFunction,
    NoMicroMips,
    NoMips16,
    NoReturn,
    NoSanitize,
    NoSplitStack,
    NoStackProtector,
    NoThreadSafetyAnalysis,
    NoThrow,
    NotTailCalled,
    OMPCaptureNoInit,
    OMPDeclareTargetDecl,
    OMPThreadPrivateDecl,
    ObjCBridge,
    ObjCBridgeMutable,
    ObjCBridgeRelated,
    ObjCException,
    ObjCExplicitProtocolImpl,
    ObjCIndependentClass,
    ObjCMethodFamily,
    ObjCNSObject,
    ObjCPreciseLifetime,
    ObjCRequiresPropertyDefs,
    ObjCRequiresSuper,
    ObjCReturnsInnerPointer,
    ObjCRootClass,
    ObjCSubclassingRestricted,
    OpenCLIntelReqdSubGroupSize,
    OpenCLKernel,
    OpenCLUnrollHint,
    OptimizeNone,
    Override,
    Ownership,
    Packed,
    ParamTypestate,
    Pascal,
    Pcs,
    PragmaClangBSSSection,
    PragmaClangDataSection,
    PragmaClangRodataSection,
    PragmaClangTextSection,
    PreserveAll,
    PreserveMost,
    PtGuardedBy,
    PtGuardedVar,
    Pure,
    RISCVInterrupt,
    RegCall,
    ReleaseCapability,
    ReqdWorkGroupSize,
    RequireConstantInit,
    RequiresCapability,
    Restrict,
    ReturnTypestate,
    ReturnsNonNull,
    ReturnsTwice,
    ScopedLockable,
    Section,
    SelectAny,
    Sentinel,
    SetTypestate,
    SharedTrylockFunction,
    StdCall,
    SwiftCall,
    SysVABI,
    TLSModel,
    Target,
    TestTypestate,
    ThisCall,
    TransparentUnion,
    TrivialABI,
    TryAcquireCapability,
    TypeTagForDatatype,
    TypeVisibility,
    Unavailable,
    Unused,
    Used,
    Uuid,
    VecReturn,
    VecTypeHint,
    VectorCall,
    Visibility,
    WarnUnused,
    WarnUnusedResult,
    Weak,
    WeakImport,
    WeakRef,
    WorkGroupSizeHint,
    X86ForceAlignArgPointer,
    XRayInstrument,
    XRayLogArgs,
    AbiTag,
    Alias,
    AlignValue,
    IFunc,
    InitSeg,
    LoopHint,
    Mode,
    NoEscape,
    OMPCaptureKind,
    OMPDeclareSimdDecl,
    OMPReferencedVar,
    ObjCBoxable,
    ObjCDesignatedInitializer,
    ObjCRuntimeName,
    ObjCRuntimeVisible,
    OpenCLAccess,
    Overloadable,
    RenderScriptKernel,
    Thread;

    private static final Lazy<EnumHelperWithValue<AttributeKind>> ENUM_HELPER = EnumHelperWithValue
            .newLazyHelperWithValue(AttributeKind.class);

    public static EnumHelperWithValue<AttributeKind> getHelper() {
        return ENUM_HELPER.get();
    }

    private static final Set<AttributeKind> IS_INLINE = new HashSet<>(Arrays.asList(OpenCLKernel));
    private static final Set<AttributeKind> IS_LOWERCASE = new HashSet<>(Arrays.asList(NonNull));

    private static final CachedItems<AttributeKind, String> ATTRIBUTE_NAMES = new CachedItems<>(
            AttributeKind::toAttributeName);

    @Override
    public String getString() {
        return name();
    }

    public String getAttributeName() {
        return ATTRIBUTE_NAMES.get(this);
    }

    private static String toAttributeName(AttributeKind kind) {
        String name = kind.name();

        // Make certain expressions lower-case
        if (name.contains("OpenCL")) {
            name = name.replace("OpenCL", "Opencl");
        }
        if (IS_LOWERCASE.contains(kind)) {
            return name.toLowerCase();
        }

        return SpecsStrings.camelCaseSeparate(name, "_").toLowerCase();
    }

    /**
     * 
     * @return true if this attribute kind should be written inlined in the code (e.g., kernel)
     */
    public boolean isInline() {
        return IS_INLINE.contains(this);
    }

}
