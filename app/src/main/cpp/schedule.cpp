#include "schedule.h"

namespace water {

std::vector<int> computeSlots(int startMin, int endMin, int schoolStart, int schoolEnd,
                              bool useSchool, int count) {
    std::vector<int> freeMinutes;
    for (int m = startMin < 0 ? 0 : startMin; m <= endMin && m < 1440; ++m) {
        if (useSchool && m >= schoolStart && m <= schoolEnd) continue;
        freeMinutes.push_back(m);
    }

    std::vector<int> slots;
    if (count <= 0 || freeMinutes.empty()) return slots;

    const int available = static_cast<int>(freeMinutes.size());
    if (count > available) count = available;

    slots.reserve(count);
    for (int i = 0; i < count; ++i) {
        // Μέσο του i-οστού από τα `count` ίσα τμήματα των ελεύθερων λεπτών.
        const int index = static_cast<int>((static_cast<long long>(2 * i + 1) * available) / (2LL * count));
        slots.push_back(freeMinutes[index]);
    }
    return slots;
}

}  // namespace water
