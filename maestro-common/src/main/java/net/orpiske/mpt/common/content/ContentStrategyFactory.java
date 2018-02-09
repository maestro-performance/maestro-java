package net.orpiske.mpt.common.content;

/**
 * Creates the appropriate {@link ContentStrategy} based on a size specification
 */
public class ContentStrategyFactory {

    private ContentStrategyFactory() {}

    /**
     * Parse a content size specification string and creates the respective ContentStrategy.
     * @param sizeSpec The size specification string {@link ContentStrategy#setSize(String)}
     * @return A ContentStrategy instance for the size spec string
     */
    public static ContentStrategy parse(final String sizeSpec) {
        ContentStrategy ret;

        if (sizeSpec.startsWith("~")) {
            ret = new VariableSizeContent();
        }
        else {
            ret = new FixedSizeContent();
        }

        ret.setSize(sizeSpec);

        return ret;
    }
}
