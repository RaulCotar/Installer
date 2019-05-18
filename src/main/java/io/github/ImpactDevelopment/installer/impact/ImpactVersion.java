package io.github.ImpactDevelopment.installer.impact;

import io.github.ImpactDevelopment.installer.GPG;
import io.github.ImpactDevelopment.installer.Installer;
import io.github.ImpactDevelopment.installer.github.GithubRelease;

/**
 * A version of Impact that we know about but might not have fetched the actual JSON for yet
 */
public class ImpactVersion {
    public final String impactVersion;
    public final String mcVersion;
    public final GithubRelease release;

    protected ImpactJsonVersion fetchedContents;

    public ImpactVersion(GithubRelease release) {
        this.impactVersion = release.tagName.split("-")[0];
        this.mcVersion = release.tagName.split("-")[1];
        this.release = release;
    }

    private String jsonFileName() {
        return "Impact-" + impactVersion + "-" + mcVersion + ".json";
    }

    public ImpactJsonVersion fetchContents() {
        if (fetchedContents == null) {
            System.out.println("Verifying GPG signatures on Impact release " + release.tagName);
            if (!GPG.verifyRelease(release, jsonFileName(), jsonFileName() + ".asc", sigs -> sigs.size() >= 2 || sigs.contains(GPG.leijurv))) {
                throw new RuntimeException("Invalid signature on Impact release " + release.tagName);
            }
            fetchedContents = Installer.gson.fromJson(release.byName(jsonFileName()).get().fetch(), ImpactJsonVersion.class);
        }
        sanityCheck();
        return fetchedContents;
    }

    public boolean possiblySigned() {
        return release.byName(jsonFileName() + ".asc").isPresent();
    }

    private void sanityCheck() {
        // make sure that the json is what it should be
        if (!fetchedContents.mcVersion.equals(mcVersion)) {
            throw new IllegalStateException(fetchedContents.mcVersion + " " + mcVersion);
        }
        if (!fetchedContents.version.equals(impactVersion)) {
            throw new IllegalStateException(fetchedContents.version + " " + impactVersion);
        }
        if (!fetchedContents.name.equals(Installer.project)) {
            throw new IllegalStateException(fetchedContents.name);
        }
    }
}