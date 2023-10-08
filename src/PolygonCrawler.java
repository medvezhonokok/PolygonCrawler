import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PolygonCrawler {
    public static final String POLYGON_REPO_DIR = "POLYGON_REPO_DIR";

    public static final Set<String> AVAILABLE_FILE_NAMES = Set.of(
            "codeforces-commons",
            "commons-ext",
            "csrf-prevention-filter",
            "docker-agent",
            "interop",
            "invoker-and-control",
            "jacuzzi",
            "jquery-drafts",
            "jrun",
            "nocturne",
//            "polygon",
            "riorita"
    );

    private void startCrawling() {
        Map<String, String> systemVariables = System.getenv();
        Path repositoryDir = null;

        if (systemVariables != null) {
            for (Map.Entry<String, String> entry : systemVariables.entrySet()) {
                String systemVariableName = entry.getKey();
                String systemVariablePath = entry.getValue();

                if (systemVariableName.equals(POLYGON_REPO_DIR)) {
                    repositoryDir = Path.of(systemVariablePath).getParent();
                    break;
                }
            }
        }

        if (repositoryDir != null) {
            List<File> allPolygonRepositoryDirectories = getAllDirectoriesByPath(repositoryDir);

            System.out.println("======= Pulling updates from master =======");
            checkoutToMasterAndPullUpdatesFromMaster(allPolygonRepositoryDirectories);
            System.out.println();

            System.out.println("======= Running bundles in polygon directory =======");
            runBundleBats(allPolygonRepositoryDirectories);
        } else {
            throw new NoSuchElementException("Cannot find 'POLYGON_REPO_DIR' variable.");
        }
    }

    private void runBundleBats(List<File> allPolygonRepositoryDirectories) {
        Map<File, Path> bundleFilesToTheirPaths = getAllBundleFilesByDirectory(allPolygonRepositoryDirectories);

        if (!bundleFilesToTheirPaths.isEmpty()) {
            // Show found bundles
            for (Map.Entry<File, Path> entry : bundleFilesToTheirPaths.entrySet()) {
                System.out.println("[INFO] \033[32mFound '" + entry.getKey().getName()
                        + "' in <" + entry.getValue() + ">.\033[0m");
            }

            // Run found bundles.
            for (Map.Entry<File, Path> entry : bundleFilesToTheirPaths.entrySet()) {
                Runner.execute("runner.bat",
                        entry.getValue().getParent().toString(),
                        "\"" + entry.getKey().getName() + "\"");
            }
        } else {
            System.out.println("[ERROR] \033[31m No bundles found. \033[0m");
        }
    }

    private Map<File, Path> getAllBundleFilesByDirectory(List<File> allPolygonRepositoryDirectories) {
        Map<File, Path> bundleFilesToTheirPaths = new HashMap<>();
        for (File file : allPolygonRepositoryDirectories) {
            if (file.isDirectory()) {
                File foundBundleBat = findFileInDirectoryByNameOrNull("bundle.bat", getChildrenFiles(file));
                File foundBundleSkipTestsBat = findFileInDirectoryByNameOrNull("bundle-skip-tests.bat", getChildrenFiles(file));

                if (foundBundleBat != null) {
                    bundleFilesToTheirPaths.put(foundBundleBat.toPath().toFile(), foundBundleBat.toPath());
                }

                if (foundBundleSkipTestsBat != null) {
                    bundleFilesToTheirPaths.put(foundBundleSkipTestsBat.toPath().toFile(), foundBundleSkipTestsBat.toPath());
                }
            }
        }

        return bundleFilesToTheirPaths;
    }

    private File findFileInDirectoryByNameOrNull(String fileName, List<File> directory) {
        if (directory != null) {
            for (File file : directory) {
                if (file.isFile() && file.getName().equals(fileName)) {
                    return file;
                } else if (file.isDirectory()) {
                    return findFileInDirectoryByNameOrNull(fileName, getChildrenFiles(file));
                }
            }
        }

        return null;
    }

    private List<File> getChildrenFiles(File directory) {
        List<File> childrens = new ArrayList<>();

        if (directory != null && directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile()) {
                    childrens.add(file);
                } else if (file.isDirectory()) {
                    childrens.addAll(getChildrenFiles(file).stream().filter(Objects::nonNull).toList());
                }
            }
        }

        return childrens;
    }

    private void checkoutToMasterAndPullUpdatesFromMaster(List<File> allPolygonRepositoryDirectories) {
        for (File directory : allPolygonRepositoryDirectories) {
            Runner.execute("git-merge-and-pull.bat", directory.getAbsolutePath());
        }
    }

    private List<File> getAllDirectoriesByPath(Path polygonRepositoryDir) {
        List<File> filesInRepositoryDir = new ArrayList<>();

        try (DirectoryStream<Path> currentDirectory = Files.newDirectoryStream(polygonRepositoryDir)) {
            for (Path pathDirectoryOrFile : currentDirectory) {
                if (AVAILABLE_FILE_NAMES.contains(pathDirectoryOrFile.toFile().getName())) {
                    filesInRepositoryDir.add(pathDirectoryOrFile.toFile());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }

        return filesInRepositoryDir;
    }

    public static void main(String[] args) {
        new PolygonCrawler().startCrawling();
    }
}
