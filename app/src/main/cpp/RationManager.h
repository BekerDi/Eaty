#pragma once
#include <string>
#include <vector>

class RationManager {
public:
    static std::string getRationTitle(const std::string& rationId);
    static std::string getRationShortDescription(const std::string& rationId);
    static std::string getRationFullPlan(const std::string& rationId);
};
