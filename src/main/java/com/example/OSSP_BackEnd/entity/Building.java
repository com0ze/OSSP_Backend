package com.example.OSSP_BackEnd.entity;

import java.util.List;

/**
 * 동국대학교 건물 Enum
 * Ray-Casting 알고리즘을 활용한 지오펜싱(Geofencing) 구현
 */
public enum Building {
    INFO_CULTURE("정보문화관", List.of(
            new Coordinate(37.5603386, 126.9983113),
            new Coordinate(37.5601445, 126.9988615),
            new Coordinate(37.5599687, 126.998703),
            new Coordinate(37.5595234, 126.9988384),
            new Coordinate(37.5592791, 126.9986541),
            new Coordinate(37.5592837, 126.9985072),
            new Coordinate(37.559528, 126.9984496),
            new Coordinate(37.5595234, 126.9982364),
            new Coordinate(37.5597769, 126.9982508),
            new Coordinate(37.5597655, 126.9980377),
            new Coordinate(37.5599185, 126.998029),
            new Coordinate(37.5599299, 126.9982019),
            new Coordinate(37.5600418, 126.9981529),
            new Coordinate(37.5603386, 126.9983113)
    )),
    WONHEUNG("원흥관", List.of(
            new Coordinate(37.5595334, 126.9984459),
            new Coordinate(37.5595182, 126.9981949),
            new Coordinate(37.5591035, 126.9982619),
            new Coordinate(37.558783, 126.9986969),
            new Coordinate(37.5585005, 126.9985819),
            new Coordinate(37.5583668, 126.999082),
            new Coordinate(37.5588392, 126.999287),
            new Coordinate(37.5590397, 126.9992334),
            new Coordinate(37.5591111, 126.9992602),
            new Coordinate(37.5595334, 126.9988387),
            new Coordinate(37.5592843, 126.9986509),
            new Coordinate(37.5592888, 126.998511),
            new Coordinate(37.5595334, 126.9984459)
    )),
    MANHAE_PLAZA("만해광장", List.of(
            new Coordinate(37.5601505, 126.9988541),
            new Coordinate(37.5603146, 126.9990057),
            new Coordinate(37.5600396, 127.000067),
            new Coordinate(37.5592283, 126.99993),
            new Coordinate(37.5591821, 126.9998863),
            new Coordinate(37.5591113, 126.9992544),
            new Coordinate(37.5595294, 126.9988374),
            new Coordinate(37.5599713, 126.9986982),
            new Coordinate(37.5601505, 126.9988541)
    )),
    HAKRIM("학림관", List.of(
            new Coordinate(37.5601473, 126.9988492),
            new Coordinate(37.5602331, 126.9986064),
            new Coordinate(37.5605813, 126.998911),
            new Coordinate(37.5606548, 126.9992333),
            new Coordinate(37.5604448, 127.0000941),
            new Coordinate(37.5604728, 127.0005179),
            new Coordinate(37.5600354, 127.0003634),
            new Coordinate(37.5600844, 127.0000897),
            new Coordinate(37.5600406, 127.0000632),
            new Coordinate(37.5603118, 126.9990081),
            new Coordinate(37.5601473, 126.9988492)
    )),
    GEUMGANG("금강관", List.of(
            new Coordinate(37.5591842, 126.9998723),
            new Coordinate(37.5592194, 127.0003131),
            new Coordinate(37.5599118, 127.0005678),
            new Coordinate(37.5598669, 127.0007862),
            new Coordinate(37.5602751, 127.0008359),
            new Coordinate(37.5604948, 127.0007476),
            new Coordinate(37.5604743, 127.000515),
            new Coordinate(37.5600389, 127.000362),
            new Coordinate(37.5600884, 127.0000863),
            new Coordinate(37.560044, 127.0000622),
            new Coordinate(37.5592298, 126.9999292),
            new Coordinate(37.5591842, 126.9998723)
    )),
    MAIN_BUILDING("본관", List.of(
            new Coordinate(37.5583752, 126.9990737),
            new Coordinate(37.5582283, 126.9991064),
            new Coordinate(37.5580658, 126.9996516),
            new Coordinate(37.55893, 127.0000478),
            new Coordinate(37.5591467, 126.9999751),
            new Coordinate(37.559197, 126.9999899),
            new Coordinate(37.5591148, 126.9992562),
            new Coordinate(37.5590409, 126.9992297),
            new Coordinate(37.5588412, 126.9992848),
            new Coordinate(37.5583752, 126.9990737)
    )),
    PALJEONGDO("팔정도", List.of(
            new Coordinate(37.5589256, 127.0000472),
            new Coordinate(37.5588318, 127.0004159),
            new Coordinate(37.5587963, 127.000407),
            new Coordinate(37.558683, 127.0007131),
            new Coordinate(37.5584988, 127.0009007),
            new Coordinate(37.5583429, 127.0007376),
            new Coordinate(37.5577974, 127.0005164),
            new Coordinate(37.55804, 126.9996406),
            new Coordinate(37.5589256, 127.0000472)
    )),
    SHINGONG("신공학관", List.of(
            new Coordinate(37.5585044, 126.9985812),
            new Coordinate(37.5585682, 126.9983396),
            new Coordinate(37.5586278, 126.9983396),
            new Coordinate(37.5587101, 126.9980533),
            new Coordinate(37.5586874, 126.9980211),
            new Coordinate(37.5587101, 126.9979245),
            new Coordinate(37.5584916, 126.9975541),
            new Coordinate(37.5583441, 126.9975165),
            new Coordinate(37.5579753, 126.9978762),
            new Coordinate(37.5578037, 126.9983772),
            new Coordinate(37.557859, 126.9986456),
            new Coordinate(37.5583725, 126.999075),
            new Coordinate(37.5585044, 126.9985812)
    )),
    CENTRAL_LIBRARY("중앙도서관", List.of(
            new Coordinate(37.5578629, 126.9986408),
            new Coordinate(37.5575158, 126.9987731),
            new Coordinate(37.5574061, 126.9991865),
            new Coordinate(37.5575917, 126.999325),
            new Coordinate(37.5580114, 126.9994879),
            new Coordinate(37.5580411, 126.9996407),
            new Coordinate(37.5580677, 126.9996538),
            new Coordinate(37.5582292, 126.9991061),
            new Coordinate(37.5583729, 126.9990755),
            new Coordinate(37.5578629, 126.9986408)
    )),
    MYUNGJIN("명진관", List.of(
            new Coordinate(37.558042, 126.9996354),
            new Coordinate(37.5577979, 127.0005173),
            new Coordinate(37.5577179, 127.0006053),
            new Coordinate(37.5572921, 127.0004934),
            new Coordinate(37.5575907, 126.9993191),
            new Coordinate(37.558014, 126.9994853),
            new Coordinate(37.558042, 126.9996354)
    )),
    SCIENCE("과학관", List.of(
            new Coordinate(37.5575932, 126.9993204),
            new Coordinate(37.5574076, 126.999184),
            new Coordinate(37.5574134, 126.9991066),
            new Coordinate(37.5572702, 126.9991619),
            new Coordinate(37.5571153, 126.9991121),
            new Coordinate(37.5570349, 126.9991066),
            new Coordinate(37.5569107, 126.9991951),
            new Coordinate(37.5568537, 126.9992024),
            new Coordinate(37.5567397, 126.9995509),
            new Coordinate(37.5567291, 126.9998532),
            new Coordinate(37.556957, 127.0000744),
            new Coordinate(37.5568986, 127.0004026),
            new Coordinate(37.5572961, 127.000491),
            new Coordinate(37.5575932, 126.9993204)
    )),
    MAIN_STADIUM("대운동장", List.of(
            new Coordinate(37.5567378, 126.9998478),
            new Coordinate(37.5562053, 126.9996299),
            new Coordinate(37.5558367, 127.0012879),
            new Coordinate(37.5567218, 127.0014811),
            new Coordinate(37.5569604, 127.0000702),
            new Coordinate(37.5567378, 126.9998478)
    )),
    SCULPTURE("조소관", List.of(
            new Coordinate(37.5567246, 127.0014714),
            new Coordinate(37.5565439, 127.0027059),
            new Coordinate(37.5563582, 127.0026755),
            new Coordinate(37.5561582, 127.0024262),
            new Coordinate(37.5558761, 127.0022863),
            new Coordinate(37.5558352, 127.0012859),
            new Coordinate(37.5567246, 127.0014714)
    )),
    LAW_SCHOOL("법학관", List.of(
            new Coordinate(37.5585057, 127.0008898),
            new Coordinate(37.55843, 127.0011539),
            new Coordinate(37.5582805, 127.0012272),
            new Coordinate(37.5581626, 127.0015956),
            new Coordinate(37.5567717, 127.0011682),
            new Coordinate(37.5569042, 127.0003915),
            new Coordinate(37.5577168, 127.0005992),
            new Coordinate(37.5577991, 127.0005089),
            new Coordinate(37.5583575, 127.0007437),
            new Coordinate(37.5585057, 127.0008898)
    )),
    HYEHWA("혜화관", List.of(
            new Coordinate(37.558169, 127.0015795),
            new Coordinate(37.5581994, 127.0019373),
            new Coordinate(37.5580238, 127.0024272),
            new Coordinate(37.5566425, 127.0020694),
            new Coordinate(37.5567743, 127.0011705),
            new Coordinate(37.558169, 127.0015795)
    )),
    SOCIAL_SCIENCE("사회과학관", List.of(
            new Coordinate(37.5565526, 127.0027028),
            new Coordinate(37.5572651, 127.0035141),
            new Coordinate(37.5574455, 127.0027028),
            new Coordinate(37.5585097, 127.0029654),
            new Coordinate(37.558283, 127.0026211),
            new Coordinate(37.5583801, 127.0023818),
            new Coordinate(37.5581488, 127.0021017),
            new Coordinate(37.5580239, 127.002411),
            new Coordinate(37.5566451, 127.0020608),
            new Coordinate(37.5565526, 127.0027028)
    )),
    CULTURE("문화관", List.of(
            new Coordinate(37.5585169, 127.0029414),
            new Coordinate(37.5588315, 127.0033208),
            new Coordinate(37.5588222, 127.0035659),
            new Coordinate(37.5581837, 127.0038869),
            new Coordinate(37.5572584, 127.0035134),
            new Coordinate(37.5574435, 127.0026905),
            new Coordinate(37.5585169, 127.0029414)
    ));

    private final String displayName;
    private final List<Coordinate> polygon;

    Building(String displayName, List<Coordinate> polygon) {
        this.displayName = displayName;
        this.polygon = polygon;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Ray-Casting 알고리즘을 사용하여 주어진 좌표가 건물의 다각형 내부에 있는지 판별
     * * @param targetLat 대상 위도
     * @param targetLng 대상 경도
     * @return 다각형 내부에 있으면 true, 아니면 false
     */
    public boolean contains(double targetLat, double targetLng) {
        // 빈 다각형인 경우 false 반환
        if (polygon == null || polygon.isEmpty()) {
            return false;
        }

        int intersectCount = 0;
        int vertexCount = polygon.size();

        for (int i = 0; i < vertexCount; i++) {
            Coordinate current = polygon.get(i);
            Coordinate next = polygon.get((i + 1) % vertexCount);

            // 수평선(ray)과 다각형의 변(edge)이 교차하는지 확인
            if (isIntersect(targetLat, targetLng, current, next)) {
                intersectCount++;
            }
        }

        // 교차 횟수가 홀수면 내부, 짝수면 외부
        return (intersectCount % 2) == 1;
    }

    /**
     * 점에서 오른쪽으로 그은 수평선이 두 점을 연결하는 선분과 교차하는지 확인
     */
    private boolean isIntersect(double targetLat, double targetLng, Coordinate p1, Coordinate p2) {
        // y 좌표(위도)가 타겟보다 위와 아래에 하나씩 있어야 함
        if ((p1.lat > targetLat) == (p2.lat > targetLat)) {
            return false;
        }

        // 교차점의 x 좌표(경도) 계산
        double intersectLng = (p2.lng - p1.lng) * (targetLat - p1.lat) / (p2.lat - p1.lat) + p1.lng;

        // 교차점이 타겟 지점보다 오른쪽에 있어야 함
        return targetLng < intersectLng;
    }

    /**
     * 주어진 좌표에 해당하는 건물을 찾아 반환
     * * @param latitude 위도
     * @param longitude 경도
     * @return 해당하는 건물, 없으면 null
     */
    public static Building findByCoordinate(double latitude, double longitude) {
        for (Building building : Building.values()) {
            if (building.contains(latitude, longitude)) {
                return building;
            }
        }
        return null;
    }

    /**
     * 좌표를 나타내는 정적 중첩 클래스
     */
    public static class Coordinate {
        public final double lat;  // 위도
        public final double lng;  // 경도

        public Coordinate(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public static Coordinate of(double lat, double lng) {
            return new Coordinate(lat, lng);
        }
    }
}