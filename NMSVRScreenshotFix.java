
package nmsvrscreenshotfix;

import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;

/**
 * Iterates through every image in the executable's folder and if applicable, 
 * creates a copy of it with an aspect ratio to 1:1
 * @author Noah Ortega
 */
public class NMSVRScreenshotFix {    
    
    public static void main(String[] args) {
        LogicController.getInstance();
    }
}

class LogicController {
    
    public boolean isExecuting = false;
    public boolean canceled = false;
    
    private int totalFiles;
    private int filesProcessed = 0;
    private BufferedImage curImage;
    
    public String sourcePath;
    public String resultPath;
 
    public boolean shouldRename = true;
    public boolean renameNewFile = true;
    public String addToFile = "_fix";
    public boolean addAsPrefix = false;
    
    ProgramUI myUI;
    
    //singleton
    private static LogicController sharedController = null;
    private LogicController() {
        launchUI();
        sourcePath = System.getProperty("user.dir");
        resultPath = System.getProperty("user.dir");
    }
    public static LogicController getInstance() {
        if(sharedController == null) {
            sharedController = new LogicController();
        }
        return sharedController;
    }
    
    private void launchUI() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProgramUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProgramUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProgramUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProgramUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               myUI = new ProgramUI();
               myUI.setVisible(true);
            }
        });
    }   
    
    /**
     * Iterates through the files in the directory, validates files before 
     * allowing resizing.
     */
    public void execute() {
        isExecuting = true;
        canceled = false;
        filesProcessed = 0;
        
        myUI.updateProgressBar(0);
        
        File sourceFolder = new File(sourcePath); //current directory
        File destFolder = new File(resultPath);
        String curFilePath;
        
        System.out.println("searching in " + sourceFolder.getPath());
        System.out.println("outputing to " + destFolder.getPath());
        
        File[] folderContents = sourceFolder.listFiles();
        totalFiles = folderContents.length;
        System.out.println("total files: " + totalFiles);

        for (int fileIndex = 0; fileIndex < totalFiles && !canceled; fileIndex++) {
            curFilePath = folderContents[fileIndex].getPath();
            try {
                if (!folderContents[fileIndex].isDirectory() && isImage(curFilePath)) {
                    filesProcessed++;
                    System.out.print(filesProcessed +". ");
                    System.out.println(curFilePath);
                    
                    curImage = ImageIO.read(folderContents[fileIndex]);

                    if (shouldResize(curImage.getWidth(), curImage.getHeight())) {
                        squish(curFilePath);
                    }
                }
            }
            catch (IOException e){
                System.err.println(">> Caught IOException on file '" + curFilePath + "':\n  " + e.getMessage());
            }
            myUI.updateProgressBar((fileIndex + 1)*100/totalFiles);
        }
        isExecuting = false;
        System.out.println("finished");
    }

    public void cancelExecution() {
        canceled = true;
    }
    
    public String getCurrentBehavior() {
        String behavior = "";
        if(!shouldRename && (sourcePath.equals(resultPath))) {
            behavior += "• Replacing originals with converted screenshots";
        }
        else {
            behavior += "• Making copies of converted screenshots";
        }
        
        behavior +="\n";
        if(shouldRename) {
            if(addAsPrefix) {
                behavior += "• Adding prefix ";
            }
            else {
                behavior += "• Adding suffix ";
            }
            behavior += "\""+addToFile+"\" to ";
            if(renameNewFile) {
                behavior += "converted image";
            }
            else {
                behavior += "original image";
            }
            behavior += "\n";
            behavior += "• " + getExampleRename();
        }
        return behavior;
    }
    
    public String getRename(String oldFileName) {
        return addAsPrefix ? (addToFile+oldFileName) : (oldFileName+addToFile);
    }
    
    public String getExampleRename() {
        String exampleName = renameNewFile ? "converted" : "original";
        return ("Ex: \"" + exampleName + ".png\" -> \"" + getRename(exampleName) + ".png\"");
    }  
    
    /**
     * Determines if a file is a basic image type
     * @param path file path of image
     * @return true if extension is .png .jpg or .jpeg
     */
    private boolean isImage(String path) {
        //extention of file, from file after final period
        int dotIndex = path.lastIndexOf('.');
        String extension = (dotIndex == -1) ? "no extension" : path.substring(dotIndex + 1);
        
        return (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg"));
    }
    
    /**
     * Determines if an image should be resized based on criteria:
     * - width must be greater than height
     * @param width image width
     * @param height image height
     * @return true if image matches criteria
     */
    private boolean shouldResize(int width, int height) {
        if (width == height) {
            System.out.println(">> Already 1:1 aspect ratio");
            return false;
        } else if (width < height) {
            System.out.println(">> Height is greater than width");
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Resizes an image to a 1:1 aspect ratio by changing the width
     * @param inputImagePath Path of the original input image
     * @throws java.io.IOException
     * 
     * based on code by Nam Ha Minh from article "How to resize images in Java"
     * https://www.codejava.net/java-se/graphics/how-to-resize-images-in-java
     */
    private void squish(String inputImagePath) throws IOException {

        int finalWidth = curImage.getHeight();
        int height = curImage.getHeight();
        
        // creates output image
        BufferedImage outputImage = new BufferedImage(finalWidth,
        height, curImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(curImage, 0, 0, finalWidth, height, null);
        g2d.dispose();

        // extracts extension of output file
        int dotIndex = inputImagePath.lastIndexOf('.');
        String formatName = inputImagePath.substring(dotIndex + 1);
        
        String outputImagePath = generateOutputPath(inputImagePath, dotIndex);
        
        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));

        System.out.println(">> Converted");
    }
    
    /**
     * For generating the path of the output image
     * @param inputPath Path of the original input image
     * @param dotIndex the index of the dot in the input path
     * @return the final output path
     */
    public String generateOutputPath(String inputPath, int dotIndex) {
        return inputPath.substring(0, dotIndex) + addToFile + inputPath.substring(dotIndex);
    }
}