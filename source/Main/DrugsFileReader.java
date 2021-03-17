/*
Signalling Visualisation Toolkit (SiViT)
Copyright (C) 2021  Abertay University

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License or any later
version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package Main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DrugsFileReader {

    private String path = "data/drugs.ini";
    private HashMap drugSet = new HashMap();

    public boolean readFile() {
        try {
            for (String line : Files.readAllLines(Paths.get(path), Charset.defaultCharset())) {
                boolean isValue = false;
                String name = "";
                int val = 0;
                for (String part : line.split(":")) {
                    if (isValue) {
                        val = Integer.decode(part);
                    } else {
                        name = part;
                    }
                    isValue = true;
                }
                drugSet.put(name, val);
            }
        } catch (IOException ex) {
            Logger.getLogger(DrugsFileReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        System.out.println("Drug set created: " + drugSet.toString());
        return true;
    }

    public HashMap getDrugSet() {
        return drugSet;
    }
}
