package net.sf.sevenzipjbinding;

/**
 * Configures the compression method used to create or update an archive.
 */
public interface IOutFeatureSetCompressionMethod {

    /**
     * Sets the compression method.
     *
     * @param compressionMethod method supported by the target archive format
     * @throws SevenZipException if the method cannot be configured
     */
    void setCompressionMethod(OutArchiveCompressionMethod compressionMethod) throws SevenZipException;
}
