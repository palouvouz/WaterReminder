// Δοκιμές του υπολογισμού ωρών. Τρέχουν στον υπολογιστή, χωρίς Android.
//   g++ -std=c++17 -I app/src/main/cpp native-tests/schedule_test.cpp app/src/main/cpp/schedule.cpp -o native-tests/schedule_test
#include <algorithm>
#include <cstdio>
#include <vector>

#include "schedule.h"

static int failures = 0;

#define CHECK(cond, msg)                                         \
    do {                                                         \
        if (!(cond)) {                                           \
            std::printf("FAIL: %s\n", msg);                      \
            ++failures;                                          \
        } else {                                                 \
            std::printf("ok:   %s\n", msg);                      \
        }                                                        \
    } while (0)

static bool sortedAndInside(const std::vector<int> &s, int lo, int hi) {
    if (!std::is_sorted(s.begin(), s.end())) return false;
    return s.empty() || (s.front() >= lo && s.back() <= hi);
}

int main() {
    {
        auto s = water::computeSlots(480, 1320, 495, 854, false, 8);
        CHECK(s.size() == 8, "8 υπενθυμίσεις χωρίς σχολείο");
        CHECK(sortedAndInside(s, 480, 1320), "ταξινομημένες και μέσα στο παράθυρο");
        int minGap = 9999, maxGap = 0;
        for (size_t i = 1; i < s.size(); ++i) {
            int g = s[i] - s[i - 1];
            minGap = std::min(minGap, g);
            maxGap = std::max(maxGap, g);
        }
        CHECK(maxGap - minGap <= 1, "ισόποσες αποστάσεις (διαφορά <= 1 λεπτό)");
    }
    {
        auto s = water::computeSlots(480, 1320, 495, 854, true, 8);
        bool inSchool = std::any_of(s.begin(), s.end(), [](int m) { return m >= 495 && m <= 854; });
        CHECK(s.size() == 8, "8 υπενθυμίσεις με σχολείο");
        CHECK(!inSchool, "καμία υπενθύμιση μέσα στις σχολικές ώρες");
        CHECK(sortedAndInside(s, 480, 1320), "με σχολείο: ταξινομημένες και μέσα στο παράθυρο");
    }
    CHECK(water::computeSlots(1000, 1002, 0, 0, false, 10).size() == 3, "count > ελεύθερα λεπτά: περιορίζεται σε 3");
    CHECK(water::computeSlots(900, 800, 0, 0, false, 5).empty(), "έναρξη μετά τη λήξη: καμία");
    CHECK(water::computeSlots(480, 1320, 0, 0, false, 0).empty(), "count 0: καμία");
    CHECK(water::computeSlots(500, 800, 495, 854, true, 5).empty(), "σχολείο καλύπτει όλο το παράθυρο: καμία");
    {
        auto s = water::computeSlots(1400, 2000, 0, 0, false, 4);
        CHECK(!s.empty() && s.back() <= 1439, "λήξη πάνω από 1439 κόβεται στο τέλος της ημέρας");
    }
    {
        auto s = water::computeSlots(-50, 100, 0, 0, false, 3);
        CHECK(!s.empty() && s.front() >= 0, "αρνητική έναρξη κόβεται στο 0");
    }
    std::printf(failures == 0 ? "\nΌλες οι δοκιμές πέρασαν\n" : "\nΑποτυχίες: %d\n", failures);
    return failures == 0 ? 0 : 1;
}
