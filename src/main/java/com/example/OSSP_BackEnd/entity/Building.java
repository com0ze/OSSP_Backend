package com.example.OSSP_BackEnd.entity;

/**
 * 동국대학교 건물 Enum (경량화 및 직관적 이름 버전)
 * 프론트엔드에서 지오펜싱 처리 후 건물명만 전달받음
 */
public enum Building {
    INFO_CULTURE("정보문화관"),
    WONHEUNG("원흥관"),
    MANHAE_PLAZA("만해광장"),
    HAKRIM("학림관"),
    GEUMGANG("금강관"),
    MAIN_BUILDING("본관"),
    PALJEONGDO("팔정도"),
    SHINGONG("신공학관"),
    CENTRAL_LIBRARY("중앙도서관"),
    MYUNGJIN("명진관"),
    SCIENCE("과학관"),
    MAIN_STADIUM("대운동장"),
    SCULPTURE("조소관"),
    LAW_SCHOOL("법학관"),
    HYEHWA("혜화관"),
    SOCIAL_SCIENCE("사회과학관"),
    CULTURE("문화관");

    private final String displayName;

    Building(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 문자열로 받은 건물명을 Enum으로 변환
     * * @param buildingName 건물 이름 (Enum 상수명)
     * @return 해당하는 Building Enum, 없으면 null
     */
    public static Building fromString(String buildingName) {
        if (buildingName == null || buildingName.trim().isEmpty()) {
            return null;
        }

        try {
            return Building.valueOf(buildingName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}