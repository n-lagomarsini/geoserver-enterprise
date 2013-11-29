package org.geoserver.wps.raster.algebra;

import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandMergeDescriptor;

import org.apache.commons.io.FileUtils;
import org.geoserver.wps.WPSTestSupport;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.process.ProcessException;

import com.sun.media.jai.util.SunTileCache;

/**
 * Test-class used for evaluating the {@link JiffleScriptProcess} class.
 * 
 * @author Nicola Lagomarsini GeoSolutions SAS
 * 
 */
public class JiffleScriptProcessTest extends WPSTestSupport {

    private static final String JIFFLE_SCRIPT_PATH = "./src/test/resources/script_jiffle";

    private static final String WRONG_BAND_MSG = "Band Index is not correct";

    private final static Logger LOGGER = Logger.getLogger(JiffleScriptProcessTest.class.toString());

    private static final String IMAGE_NAME_1 = "srtm_39_04_1.tiff";

    private static final String IMAGE_NAME_2 = "srtm_39_04_2.tiff";

    private static final String IMAGE_NAME_3 = "multi_banded.tiff";

    private static final int WRONG_NUM_BANDS = 10;

    private static final int FINAL_NUM_BANDS = 1;

    private final static JiffleScriptProcess process = new JiffleScriptProcess();

    private static GridCoverage2D testCoverage1;

    private static GridCoverage2D testCoverage2;

    private static GridCoverage2D testCoverage3;

    private RenderingHints hints;

    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();

        GeoTiffFormat format = new GeoTiffFormat();

        hints = new RenderingHints(JAI.KEY_TILE_CACHE, new SunTileCache(1024 * 1024 * 1024));
        // Image preparation
        if (testCoverage1 == null) {

            GeoTiffReader reader = null;

            try {
                final File input = new File(getClass().getResource(IMAGE_NAME_1).getPath());
                reader = format.getReader(input);
                testCoverage1 = reader.read(null);
            } catch (Exception e) {
                LOGGER.log(Level.FINER, e.getMessage(), e);
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
        }

        if (testCoverage2 == null) {

            GeoTiffReader reader = null;

            try {
                final File input = new File(getClass().getResource(IMAGE_NAME_2).getPath());
                reader = format.getReader(input);
                testCoverage2 = reader.read(null);
            } catch (Exception e) {
                LOGGER.log(Level.FINER, e.getMessage(), e);
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
        }

        if (testCoverage3 == null) {

            GeoTiffReader reader = null;

            try {
                final File input = new File(getClass().getResource(IMAGE_NAME_3).getPath());
                reader = format.getReader(input);
                testCoverage3 = reader.read(null);
            } catch (Exception e) {
                LOGGER.log(Level.FINER, e.getMessage(), e);
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
        }
    }

    // Test on a single one band image
    public void testSimpleScript() throws Exception {
        if (testCoverage1 != null) {
            // Script selection
            String script = FileUtils.readFileToString(new File(JIFFLE_SCRIPT_PATH));
            // Process calculation
            GridCoverage2D finalCoverage = process.execute(testCoverage1, script, null);
            // Check the result
            assertNotNull(finalCoverage);

            RenderedImage finalImage = finalCoverage.getRenderedImage();

            assertEquals(finalImage.getSampleModel().getNumBands(), FINAL_NUM_BANDS);

            PlanarImage.wrapRenderedImage(finalImage).getTiles();

            checkExecution(finalImage, testCoverage1);

            finalCoverage.dispose(true);
            if (finalImage instanceof RenderedOp) {
                ((RenderedOp) finalImage).dispose();
            }
        } else {
            LOGGER.log(
                    Level.WARNING,
                    "\nTest1: file "
                            + IMAGE_NAME_1
                            + " not found in geoserver-enterprise/src/extension/wps/wps-core/src/test/java/org/geoserver/wps/raster/algebra");
        }
    }

    // Test on 2 images merged on a single multibanded image
    public void testSimpleScript2ImagesCombined() throws Exception {

        if (testCoverage1 != null && testCoverage2 != null) {
            // Script selection
            String script = FileUtils.readFileToString(new File(JIFFLE_SCRIPT_PATH));

            // Combination of the bands of the 2 images
            RenderedImage img1 = testCoverage1.getRenderedImage();
            RenderedImage img2 = testCoverage2.getRenderedImage();

            RenderedImage imgComposed = BandMergeDescriptor.create(img1, img2, hints);

            GridCoverage2D composedCoverage = new GridCoverageFactory(null).create("New_cov",
                    imgComposed, testCoverage1.getGridGeometry().getEnvelope());
            // Process calculation
            GridCoverage2D finalCoverage = process.execute(composedCoverage, script, 0);
            // Check the result
            assertNotNull(finalCoverage);

            RenderedImage finalImage = finalCoverage.getRenderedImage();

            assertEquals(finalImage.getSampleModel().getNumBands(), FINAL_NUM_BANDS);

            PlanarImage.wrapRenderedImage(finalImage).getTiles();

            checkExecution(finalImage, testCoverage1, testCoverage2);

            finalCoverage.dispose(true);
            if (finalImage instanceof RenderedOp) {
                ((RenderedOp) finalImage).dispose();
            }
        } else {
            LOGGER.log(
                    Level.WARNING,
                    "\nTest2: files not found in geoserver-enterprise/src/extension/wps/wps-core/src/test/java/org/geoserver/wps/raster/algebra");
        }
    }

    // Test on a multibanded image
    public void testSimpleScriptMultiBandedImage() throws Exception {

        if (testCoverage3 != null) {
            // Script selection
            String script = FileUtils.readFileToString(new File(JIFFLE_SCRIPT_PATH));
            // Process calculation
            GridCoverage2D finalCoverage = process.execute(testCoverage3, script, 0);
            // Check the result
            assertNotNull(finalCoverage);

            RenderedImage finalImage = finalCoverage.getRenderedImage();

            assertEquals(finalImage.getSampleModel().getNumBands(), FINAL_NUM_BANDS);

            PlanarImage.wrapRenderedImage(finalImage).getTiles();

            checkExecution(finalImage, testCoverage3);

            finalCoverage.dispose(true);
            if (finalImage instanceof RenderedOp) {
                ((RenderedOp) finalImage).dispose();
            }
        } else {
            LOGGER.log(
                    Level.WARNING,
                    "\nTest3: file "
                            + IMAGE_NAME_3
                            + " not found in geoserver-enterprise/src/extension/wps/wps-core/src/test/java/org/geoserver/wps/raster/algebra");
        }
    }

    public void testWrongBandIndexException() throws Exception {

        if (testCoverage3 != null) {
            // Script selection
            String script = FileUtils.readFileToString(new File(JIFFLE_SCRIPT_PATH));

            GridCoverage2D finalCoverage = null;
            try {
                // Process calculation
                finalCoverage = process.execute(testCoverage3, script, WRONG_NUM_BANDS);
            } catch (ProcessException e) {
                assertEquals(e.getMessage(), WRONG_BAND_MSG);
            }
            // If a Coverage is calculated, something went wrong
            if (finalCoverage != null) {
                throw new RuntimeException("Test4: Wrong band index cannot be calculated");
            }

        } else {
            LOGGER.log(
                    Level.WARNING,
                    "\nTest4: file "
                            + IMAGE_NAME_3
                            + " not found in geoserver-enterprise/src/extension/wps/wps-core/src/test/java/org/geoserver/wps/raster/algebra");
        }
    }

    /**
     * Private method for ensuring the validity of the output image.
     * 
     * @param outputImage RenderedImage extracted from the output coverage
     * @param inputCoverages Input Coverages used.
     */
    private void checkExecution(RenderedImage outputImage, GridCoverage2D... inputCoverages) {

        RenderedImage inputImage = inputCoverages[0].getRenderedImage();

        int minTileX = outputImage.getMinTileX();
        int minTileY = outputImage.getMinTileY();
        int maxTileX = outputImage.getNumXTiles() + minTileX;
        int maxTileY = outputImage.getNumYTiles() + minTileY;

        int minX;
        int minY;
        int maxX;
        int maxY;

        Raster inputTile;
        Raster outputTile;

        int inputValue;
        int outputValue;
        // Cycle on each tile
        for (int xTile = minTileX; xTile < maxTileX; xTile++) {
            for (int yTile = minTileY; yTile < maxTileY; yTile++) {

                inputTile = inputImage.getTile(xTile, yTile);
                outputTile = outputImage.getTile(xTile, yTile);

                minX = inputTile.getMinX();
                minY = inputTile.getMinY();

                maxX = inputTile.getWidth() + minX;
                maxY = inputTile.getHeight() + minY;
                // Cycle on the x axis
                for (int x = minX; x < maxX; x++) {
                    // Cycle on the y axis
                    for (int y = minY; y < maxY; y++) {
                        inputValue = inputTile.getSample(x, y, 0);
                        outputValue = outputTile.getSample(x, y, 0);
                        // Check if the script operation is performed correctly
                        if (inputValue > 0) {
                            assertEquals(outputValue, 1);
                        } else {
                            assertEquals(outputValue, 0);
                        }
                    }
                }
            }
        }
    }
}
