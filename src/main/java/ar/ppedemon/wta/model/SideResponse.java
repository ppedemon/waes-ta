package ar.ppedemon.wta.model;

/**
 * Class modeling response to side upsert.
 *
 * @author ppedemon
 */
public class SideResponse {

    /**
     * Build left hand side response.
     * @param userId user Id
     * @param id     comparison Id
     * @return       {@link SideResponse} instance
     */
    public final static SideResponse lhsResponse(String userId, String id) {
        return new SideResponse(userId, id, "left");
    }

    /**
     * Build right hand side response.
     * @param userId user Id
     * @param id     comparison Id
     * @return       {@link SideResponse} instance
     */
    public final static SideResponse rhsResponse(String userId, String id) {
        return new SideResponse(userId, id, "right");
    }

    private final String userId;
    private final String id;
    private final String side;

    private SideResponse(String userId, String id, String side) {
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
