package com.tylermayoff.dynamicwallpaper.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CustomUtilities {

    public static int compareNatural (String s1, String s2)
    {
        // Skip all identical characters
        int len1 = s1.length();
        int len2 = s2.length();
        int i;
        char c1, c2;
        for (i = 0, c1 = 0, c2 = 0; (i < len1) && (i < len2) && (c1 = s1.charAt(i)) == (c2 = s2.charAt(i)); i++);

        // Check end of string
        if (c1 == c2)
            return(len1 - len2);

        // Check digit in first string
        if (Character.isDigit(c1))
        {
            // Check digit only in first string
            if (!Character.isDigit(c2))
                return(1);

            // Scan all integer digits
            int x1, x2;
            for (x1 = i + 1; (x1 < len1) && Character.isDigit(s1.charAt(x1)); x1++);
            for (x2 = i + 1; (x2 < len2) && Character.isDigit(s2.charAt(x2)); x2++);

            // Longer integer wins, first digit otherwise
            return(x2 == x1 ? c1 - c2 : x1 - x2);
        }

        // Check digit only in second string
        if (Character.isDigit(c2))
            return(-1);

        // No digits
        return(c1 - c2);
    }


    public static boolean unpackZip(File zipFile, File outputDir)
    {
        if (!outputDir.exists())
            outputDir.mkdir();

        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(zipFile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File f = new File(outputDir + "/" + filename);
                    f.mkdirs();
                    continue;
                }

                FileOutputStream fOut = new FileOutputStream(outputDir + "/" + filename);

                while ((count = zis.read(buffer)) != -1)
                {
                    fOut.write(buffer, 0, count);
                }

                fOut.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}