import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

public final class Runner {

    public static final Path SCRIPTS_DIR = Path.of("Z:\\PolygonCrawler\\scripts");
    public static final String DEFAULT_SCRIPT_TO_RUN = "runner.bat";
    public static final Set<String> AVAILABLE_SCRIPT_NAMES = Set.of(
            "runner.bat",
            "git-merge-and-pull.bat"
    );

    public static void execute(String scriptName, String pathToFile, String... args) {
        if (validateScript(scriptName, pathToFile, args)) {
            process(scriptName, pathToFile, args);
        }
    }

    @SuppressWarnings("deprecation")
    private static void process(String scriptName, String pathToFile, String... args) {
        Process process;

        if (scriptName == null) {
            scriptName = DEFAULT_SCRIPT_TO_RUN;
        }

        try {
            System.out.println("[INFO] Started executing command line(s) "
                    + Arrays.toString(args) + " in <" + pathToFile + ">...");

            long before = System.currentTimeMillis();
            process = Runtime.getRuntime().exec(SCRIPTS_DIR.resolve(scriptName) + " "
                    + pathToFile + " " + String.join(" ", args));

            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder log = new StringBuilder();
            boolean buildSuccess = true;

            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    if (line.contains("[ERROR]")) {
                        buildSuccess = false;
                    }
                    log.append("\033[35m[CMD OUTPUT] \033[0m").append("\033[34m ").append(line).append(" \033[0m\n");
                }
            }

            log = new StringBuilder(log.substring(0, log.length() - 1));

            if (!buildSuccess) {
                System.out.println(log);
            } else if (!"git-merge-and-pull.bat".equals(scriptName)) {
                System.out.println("[INFO] Successfully build " + args[0] + " in <" + pathToFile + ">.");
            } else {
                System.out.println(log);
            }

            process.waitFor();
            long after = System.currentTimeMillis();

            System.out.println("[INFO] Finished executing command line(s) "
                    + Arrays.toString(args) + " in <" + pathToFile + "> in [" + (after - before) + "] ms.\n");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean validateScript(String scriptName, String pathToFile, String... args) {
        if (scriptName == null) {
            scriptName = DEFAULT_SCRIPT_TO_RUN;
        }


        if (pathToFile == null) {
            throw new IllegalArgumentException("Null path to file.");
        }

        for (String arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Null arguments are not allowed.");
            }
        }

        if (!AVAILABLE_SCRIPT_NAMES.contains(scriptName)) {
            throw new IllegalArgumentException("No such script: " + scriptName);
        } else {
            return true;
        }
    }
}
