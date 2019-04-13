package com.bms.config;

import java.util.ResourceBundle;

public class ConfigReader {

    private static ResourceBundle rb = ResourceBundle.getBundle("config");

    public static String getProperty(String key){
        return ConfigReader.rb.getString(key);
    }
}
