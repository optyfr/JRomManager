package jrm.aui.basic;

import java.util.UUID;

import com.eclipsesource.json.JsonObject;

/**
 * SrcDstResult is a concrete implementation of AbstractSrcDstResult that represents a source-destination result. It includes fields
 * for the source, destination, result, and selection status, as well as methods to set and get these values. It also provides
 * constructors for creating instances from a JSON object and for initializing with source and destination values.
 */
public class SrcDstResult extends AbstractSrcDstResult {
    /**
     * The unique identifier for this SrcDstResult instance. It is generated using UUID when a new instance is created with a source
     * value.
     */
    private String id;
    /** The source value for this SrcDstResult instance. It represents the starting point or input for the result. */
    private String src;
    /** The destination value for this SrcDstResult instance. It represents the endpoint or output for the result. */
    private String dst;
    /**
     * The result value for this SrcDstResult instance. It represents the outcome or result of processing the source and destination
     * values.
     */
    private String result;
    /**
     * A flag to indicate whether this SrcDstResult instance is selected. It can be used to track the selection status of the result
     * in a user interface or other context.
     */
    private boolean selected;

    /**
     * Constructs a new SrcDstResult instance with default values. The id, src, dst, result, and selected fields are initialized to
     * their default values (null for strings and false for boolean).
     */
    public SrcDstResult() {
        super();
    }

    /**
     * Constructs a new SrcDstResult instance with the specified source value. The id is generated using UUID, and the dst, result,
     * and selected fields are initialized to their default values (null for strings and false for boolean).
     * 
     * @param src the source value to initialize this SrcDstResult instance with
     */
    public SrcDstResult(String src) {
        super();
        this.id = UUID.randomUUID().toString();
        this.src = src;
    }

    /**
     * Constructs a new SrcDstResult instance with the specified source and destination values. The id is generated using UUID, and
     * the result and selected fields are initialized to their default values (null for strings and false for boolean).
     * 
     * @param src the source value to initialize this SrcDstResult instance with
     * @param dst the destination value to initialize this SrcDstResult instance with
     */
    public SrcDstResult(String src, String dst) {
        super();
        this.id = UUID.randomUUID().toString();
        this.src = src;
        this.dst = dst;
    }

    /**
     * Constructs a new SrcDstResult instance from a JSON object. The fields of the SrcDstResult instance are initialized based on
     * the values in the provided JSON object.
     * 
     * @param jso the JSON object to initialize this SrcDstResult instance with
     */
    public SrcDstResult(JsonObject jso) {
        super(jso);
    }

    /**
     * Converts a list of SrcDstResult objects to a JSON string. The method takes a list of SrcDstResult objects and returns a JSON
     * string representation of the list.
     * 
     * @param json the JSON string to convert to a list of SrcDstResult objects
     * 
     * @return a list of SrcDstResult objects represented by the provided JSON string
     */
    public static SDRList<SrcDstResult> fromJSON(String json) {
        return fromJSON(json, SrcDstResult.class);
    }

    /**
     * The getId method is overridden to return the id field of the SrcDstResult instance. This method is used to retrieve the
     * unique identifier for this SrcDstResult instance, which can be useful for tracking and managing instances in a collection or
     * database.
     * 
     * @return the unique identifier for this SrcDstResult instance
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * The setId method is overridden to allow setting the id field of the SrcDstResult instance. This method is protected, meaning
     * it can only be accessed within the same package or by subclasses of SrcDstResult. The id field is typically generated using
     * UUID when a new instance is created, but this method allows for manual setting of the id if needed.
     * 
     * @param id the unique identifier to set for this SrcDstResult instance
     */
    @Override
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * The getSrc method is overridden to return the src field of the SrcDstResult instance. This method is used to retrieve the
     * source value for this SrcDstResult instance, which represents the starting point or input for the result.
     * 
     * @return the source value for this SrcDstResult instance
     */
    @Override
    public String getSrc() {
        return src;
    }

    /**
     * The setSrc method is overridden to allow setting the src field of the SrcDstResult instance. This method is used to update
     * the source value for this SrcDstResult instance, which represents the starting point or input for the result.
     * 
     * @param src the source value to set for this SrcDstResult instance
     */
    @Override
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * The getDst method is overridden to return the dst field of the SrcDstResult instance. This method is used to retrieve the
     * destination value for this SrcDstResult instance, which represents the endpoint or output for the result.
     * 
     * @return the destination value for this SrcDstResult instance
     */
    @Override
    public String getDst() {
        return dst;
    }

    /**
     * The setDst method is overridden to allow setting the dst field of the SrcDstResult instance. This method is used to update
     * the destination value for this SrcDstResult instance, which represents the endpoint or output for the result.
     * 
     * @param dst the destination value to set for this SrcDstResult instance
     */
    @Override
    public void setDst(String dst) {
        this.dst = dst;
    }

    /**
     * The getResult method is overridden to return the result field of the SrcDstResult instance. This method is used to retrieve
     * the result value for this SrcDstResult instance, which represents the outcome or result of processing the source and
     * destination values.
     * 
     * @return the result value for this SrcDstResult instance
     */
    @Override
    public String getResult() {
        return result;
    }

    /**
     * The setResult method is overridden to allow setting the result field of the SrcDstResult instance. This method is used to
     * update the result value for this SrcDstResult instance, which represents the outcome or result of processing the source and
     * destination values.
     * 
     * @param result the result value to set for this SrcDstResult instance
     */
    @Override
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * The isSelected method is overridden to return the selected field of the SrcDstResult instance. This method is used to check
     * whether this SrcDstResult instance is currently selected, which can be useful for tracking selection status in a user
     * interface or other context.
     * 
     * @return true if this SrcDstResult instance is selected, false otherwise
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * The setSelected method is overridden to allow setting the selected field of the SrcDstResult instance. This method is used to
     * update the selection status of this SrcDstResult instance, which can be useful for tracking selection status in a user
     * interface or other context.
     * 
     * @param selected true to mark this SrcDstResult instance as selected, false to mark it as not selected
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}