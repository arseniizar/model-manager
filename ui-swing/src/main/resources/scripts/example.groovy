package scripts

if (LL > 0 && EKS?.length == LL && PKB?.length == LL) {
    ZDEKS = new double[LL]
    for (i = 0; i < LL; i++) {
        if (PKB[i] != 0) {
            ZDEKS[i] = EKS[i] / PKB[i]
        } else {
            ZDEKS[i] = 0
        }
    }
} else {
    throw new IllegalArgumentException("Invalid input: Ensure LL, EKS, and PKB are properly initialized and have the same length.")
}
