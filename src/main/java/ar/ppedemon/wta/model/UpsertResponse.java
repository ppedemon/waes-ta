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
     * @param id     comparison Id
     * @return       {@link UpsertResponse} instance
     */
    public final static UpsertResponse rhsResponse(String userId, String id) {
        return new UpsertResponse(userId, id, "right");
    }

    private final String userId;
    private final String id;
    private final String side;

    private UpsertResponse(String userId, String id, String side) {
        this.userId = userId;
        this.id = id;
        this.side = side;
    }

    public String getUserId() {
        return userId;
    }

    public String getId() {
        return id;
    }

    public String getSide() {
        return side;
    }
}
