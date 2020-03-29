package me.deecaad.core.file;

import me.deecaad.core.utils.DebugUtil;
import me.deecaad.core.utils.LogLevel;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarSerializers extends FileCopier {

    /**
     * Method to get all serializers inside plugin's jar file.
     *
     * @param plugin the plugin instance of jar
     * @param jar the jar file of plugin
     * @param noUse the list of class names which shouldn't be used (only simple name, not whole path)
     * @return the list of all found serializers
     */
    public List<Serializer<?>> getAllSerializersInsideJar(Plugin plugin, File jar, String... noUse) {
        Set<String> keywords = new HashSet<>();
        List<Serializer<?>> serializers = new ArrayList<>();
        try {
            JarFile jarFile = getJarFile(plugin, jar);
            if (jarFile == null) {
                return null;
            }
            Class<?> serializerClass;
            try {
                serializerClass = Class.forName("me.deecaad.core.file.Serializer");
            } catch (ClassNotFoundException e) {
                DebugUtil.log(LogLevel.WARN,
                        "Could not find me.deecaad.core.file.Serializer class???",
                        e);
                return null;
            }
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entryName.endsWith(".class") || entryName.equals("me/deecaad/core/file/Serializer.class")) {
                    continue;
                }
                String nameWithoutSuffix = entryName.replaceAll("/", "\\.").replace(".class", "");
                Class<?> nameClass;
                try {
                    nameClass = Class.forName(nameWithoutSuffix);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    continue;
                }
                if (noUse != null && noUse.length > 0) {
                    boolean no = false;
                    for (String doNotUse : noUse) {
                        if (nameClass.getSimpleName().equalsIgnoreCase(doNotUse)) {
                            no = true;
                            break;
                        }
                    }
                    if (no) {
                        continue;
                    }
                }
                if (!serializerClass.isAssignableFrom(nameClass)) {
                    continue;
                }
                Constructor<?> nameConstructor;
                try {
                    nameConstructor = nameClass.getConstructor();
                } catch (NoSuchMethodException | SecurityException e) {
                    DebugUtil.log(LogLevel.ERROR,
                            "Found an class implementing serializer class which didn't have empty constructor!",
                            "Please add empty constructor for class " + nameWithoutSuffix);
                    continue;
                }
                Serializer<?> nameSerializer = (Serializer<?>) nameConstructor.newInstance();
                if (nameSerializer == null || nameSerializer.getKeyword() == null) {
                    DebugUtil.log(LogLevel.ERROR,
                            "Could not create serializer instance for class " + nameWithoutSuffix + ".",
                            "Or it didn't have keyword defined?");
                    continue;
                }

                if (keywords.contains(nameSerializer.getKeyword())) {
                    DebugUtil.log(LogLevel.ERROR,
                            "Found an duplicate keyword " + nameSerializer.getKeyword() + " from serializers.",
                            "Please change it from class " + nameWithoutSuffix + " or from the other one which had same keyword");
                    continue;
                }

                serializers.add(nameSerializer);
            }
        } catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return serializers;
    }
}