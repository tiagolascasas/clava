#include <iostream>
#include <fstream>
#include <vector>
#include <numeric>
#include <algorithm>
#include <chrono>
#include <memory>

/**
 * Code taken from ANTAREX/IT4I/Probability
 */
using result_t = std::vector<float>;
 
 template<typename... Values>
std::vector<float> get_percentiles(result_t estimated_travel_times, int input, Values...);
 
template<typename... Values>
std::vector<float> get_percentiles(result_t estimated_travel_times, int input, Values... inputs)
{
	int len = sizeof...(Values) + 1;
	int vals[] = {input, inputs...};
	std::vector<int> n(len);
	std::vector<float> results(len);
	std::copy( vals, vals+len, n.data() );
	std::sort(estimated_travel_times.begin(),estimated_travel_times.end());
	int result_len = estimated_travel_times.size();

    for(int i=0; i<len; i++)
	{
		results[i] = estimated_travel_times[result_len/100*n[i]];
	}
	return results;
}