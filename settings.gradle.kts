pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        // Заменяем сокращение на полный URL, чтобы исключить любые проблемы
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}

rootProject.name = "TheAlfred"
include(":app")