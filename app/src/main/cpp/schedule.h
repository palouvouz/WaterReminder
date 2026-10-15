#pragma once

#include <vector>

namespace water {

// Επιστρέφει `count` λεπτά της ημέρας (0..1439), σε ίσες αποστάσεις μέσα στα ελεύθερα λεπτά
// του διαστήματος [startMin, endMin]. Αν useSchool είναι true, τα λεπτά [schoolStart, schoolEnd]
// εξαιρούνται. Αν τα ελεύθερα λεπτά είναι λιγότερα από count, επιστρέφονται όσα χωράνε.
std::vector<int> computeSlots(int startMin, int endMin, int schoolStart, int schoolEnd,
                              bool useSchool, int count);

}  // namespace water
