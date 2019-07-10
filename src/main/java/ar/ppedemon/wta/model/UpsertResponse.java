package ar.ppedemon.wta.model;

/**
 * Class modeling response to side upsert.
 *
 * @author ppedemon
 */
public class UpsertResponse {

    /**
     * Build left hand side response.
     * @param userId user Id
     * @param id     comparison Id
     * @return       {@link UpsertResponse} instance
     */
    public static UpsertResponse lhsResponse(String userId, String id) {
        return new UpsertResponse(userId, id, "left");
    }

    /**
     * Build right hand side response.
     * @param userId user Id
     * @param cmpId     comparison Id
     * @return       {@link UpsertResponse} instance
     */
    public final static UpsertResponse rhsResponse(String userId, String cmpId) {
        return new UpsertResponse(userId, cmpId, "right");
    }

    private final String userId;
    private final String cmpId;
    private final String side;

    private UpsertResponse(String userId, String id, String side) {
        this.userId = userId;
        this.cmpId = id;
        this.side = side;
    }

    public String getUserId() {
        return userId;
    }

    public String getCmpId() {
        return cmpId;
    }

    public String getSide() {
        return side;
    }
}
