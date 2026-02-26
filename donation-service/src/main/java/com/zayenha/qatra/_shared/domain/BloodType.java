package com.zayenha.qatra._shared.domain;

import java.util.Set;

public enum BloodType {
    A_POSITIVE,
    A_NEGATIVE,
    B_POSITIVE,
    B_NEGATIVE,
    AB_POSITIVE,
    AB_NEGATIVE,
    O_POSITIVE,
    O_NEGATIVE,
    UNKNOWN;

    private static final Set<BloodType> O_NEGATIVE_CAN_DONATE_TO = Set.of(O_NEGATIVE);
    private static final Set<BloodType> O_POSITIVE_CAN_DONATE_TO = Set.of(O_NEGATIVE, O_POSITIVE);
    private static final Set<BloodType> A_NEGATIVE_CAN_DONATE_TO = Set.of(A_NEGATIVE, A_POSITIVE, AB_NEGATIVE, AB_POSITIVE);
    private static final Set<BloodType> A_POSITIVE_CAN_DONATE_TO = Set.of(A_POSITIVE, AB_POSITIVE);
    private static final Set<BloodType> B_NEGATIVE_CAN_DONATE_TO = Set.of(B_NEGATIVE, B_POSITIVE, AB_NEGATIVE, AB_POSITIVE);
    private static final Set<BloodType> B_POSITIVE_CAN_DONATE_TO = Set.of(B_POSITIVE, AB_POSITIVE);
    private static final Set<BloodType> AB_NEGATIVE_CAN_DONATE_TO = Set.of(AB_NEGATIVE, AB_POSITIVE);
    private static final Set<BloodType> AB_POSITIVE_CAN_DONATE_TO = Set.of(AB_POSITIVE);

    public boolean canDonateTo(BloodType recipient) {
        if (this == UNKNOWN) return false;
        return switch (this) {
            case O_NEGATIVE -> O_NEGATIVE_CAN_DONATE_TO.contains(recipient);
            case O_POSITIVE -> O_POSITIVE_CAN_DONATE_TO.contains(recipient);
            case A_NEGATIVE -> A_NEGATIVE_CAN_DONATE_TO.contains(recipient);
            case A_POSITIVE -> A_POSITIVE_CAN_DONATE_TO.contains(recipient);
            case B_NEGATIVE -> B_NEGATIVE_CAN_DONATE_TO.contains(recipient);
            case B_POSITIVE -> B_POSITIVE_CAN_DONATE_TO.contains(recipient);
            case AB_NEGATIVE -> AB_NEGATIVE_CAN_DONATE_TO.contains(recipient);
            case AB_POSITIVE -> AB_POSITIVE_CAN_DONATE_TO.contains(recipient);
            default -> false;
        };
    }
}
