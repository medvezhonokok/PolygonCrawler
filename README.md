## About:

Java-App which crawls all polygon repository directory. Each directory will be switched to branch 'master' and updated.
All 'bundle.bat' and 'bundle-skip-tests.bat' will be executed.

## Setup:

- Put path to 'PolygonCrawler/scripts' in 'Runner.SCRIPTS_DIR'
- Check 'PolygonCrawler.AVAILABLE_FILE_NAMES': each directory's branch will be switched to 'origin/master' and git pulled.

## Launch:

- When you set up crawler, just run 'PolygonCrawler.java'. 