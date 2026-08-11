package net.sf.sevenzipjbinding.junit.compression;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IOutCreateArchive;
import net.sf.sevenzipjbinding.IOutFeatureSetCompressionMethod;
import net.sf.sevenzipjbinding.IOutFeatureSetLevel;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.OutArchiveCompressionMethod;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.junit.VoidContext;
import net.sf.sevenzipjbinding.junit.tools.RandomContext;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

/**
 * Verifies compression methods added by the Android output archive bridge.
 */
public class CompressFeatureSetCompressionMethod extends CompressFeatureAbstractSingleFile<VoidContext> {
    private static final int DATA_SIZE = 32 * 1024;
    private static final int ENTROPY = 20;

    private ArchiveFormat archiveFormat;

    @Test
    public void testZipAdvancedCompressionMethods() throws Exception {
        verifyCompressionMethod(ArchiveFormat.ZIP, OutArchiveCompressionMethod.LZMA);
        verifyCompressionMethod(ArchiveFormat.ZIP, OutArchiveCompressionMethod.PPMD);
    }

    @Test
    public void testSevenZipPpmdCompressionMethod() throws Exception {
        verifyCompressionMethod(ArchiveFormat.SEVEN_ZIP, OutArchiveCompressionMethod.PPMD);
    }

    @Override
    protected ArchiveFormat getArchiveFormat() {
        return archiveFormat;
    }

    private void verifyCompressionMethod(ArchiveFormat format, OutArchiveCompressionMethod method)
            throws Exception {
        archiveFormat = format;
        RandomContext randomContext = new RandomContext(DATA_SIZE, ENTROPY);
        ByteArrayStream outputStream = new ByteArrayStream(DATA_SIZE * 2);

        IOutCreateArchive<IOutItemAllFormats> outArchive = createArchive();
        assertTrue(outArchive instanceof IOutFeatureSetCompressionMethod);
        ((IOutFeatureSetCompressionMethod) outArchive).setCompressionMethod(method);
        ((IOutFeatureSetLevel) outArchive).setLevel(5);
        outArchive.createArchive(outputStream, 1, new FeatureSingleFileCreateArchiveCallback(randomContext));
        closeArchive(outArchive);

        outputStream.rewind();
        IInArchive inArchive = SevenZip.openInArchive(format, outputStream);
        addCloseable(inArchive);
        String actualMethod = inArchive.getStringProperty(0, PropID.METHOD).toUpperCase(Locale.ROOT);
        assertTrue(actualMethod, actualMethod.contains(method.getMethodName().toUpperCase(Locale.ROOT)));
        closeArchive(inArchive);

        verifySingleFileArchive(randomContext, outputStream);
    }
}
