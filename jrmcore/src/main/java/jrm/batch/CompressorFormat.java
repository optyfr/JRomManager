package jrm.batch;

/**
 * Enum representing different compressor formats. This enum defines the available compression formats that can be used for
 * compressing data. The supported formats include ZIP, TZIP, and SEVENZIP. Each format corresponds to a specific compression
 * algorithm and file structure, allowing users to choose the appropriate format based on their requirements and preferences.
 */
public enum CompressorFormat {
    /**
     * ZIP format, a widely used compression format that supports lossless data compression. It is commonly used for archiving and
     * compressing files and directories.
     */
    ZIP,
    /**
     * Torrent ZIP (TZIP) format, a variant of the ZIP format that is optimized for use in torrent files. It is designed to
     * efficiently compress and distribute large files over peer-to-peer networks.
     */
    TZIP,
    /**
     * 7-Zip format, a high-compression format that uses the LZMA algorithm for efficient compression. It is known for its high
     * compression ratios and support for large file sizes, making it suitable for archiving and compressing large datasets.
     */
    SEVENZIP;
}
