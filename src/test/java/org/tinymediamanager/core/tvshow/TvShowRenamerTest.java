package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

public class TvShowRenamerTest extends BasicTest {

  private static final String FOLDER = getSettingsFolder();

  private static TvShow       single = new TvShow();
  private static TvShow       multi  = new TvShow();
  private static TvShow       disc   = new TvShow();
  private static TvShow       discEP = new TvShow();

  @BeforeClass
  public static void init() {
    deleteSettingsFolder();
    Settings.getInstance(FOLDER);

    // setup dummy
    MediaFile dmf = new MediaFile(new File("/path/to", "video.avi"));

    single.setTitle("singleshow");
    single.setYear("2009");
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("singleEP");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(3);
    ep.setDvdEpisode(4);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(single);
    single.addEpisode(ep);

    multi.setTitle("multishow");
    multi.setYear("2009");
    ep = new TvShowEpisode();
    ep.setTitle("multiEP2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(3);
    ep.setDvdEpisode(4);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(multi);
    multi.addEpisode(ep);
    ep = new TvShowEpisode();
    ep.setTitle("multiEP3");
    ep.setSeason(1);
    ep.setEpisode(3);
    ep.setDvdSeason(3);
    ep.setDvdEpisode(5);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(multi);
    multi.addEpisode(ep);

    disc.setTitle("Janosik");
    disc.setYear("2009");
    disc.setPath(FOLDER + "/tv/Janosik DVD");
    ep = new TvShowEpisode();
    ep.setPath(FOLDER + "/tv/Janosik DVD/Janosik S01E07E08E09");
    ep.setTvShow(disc);
    ep.setDisc(true);
    ep.setTitle("discfile");
    ep.setAiredSeason(1);
    ep.setAiredEpisode(2);
    ep.addToMediaFiles(new MediaFile(Paths.get(FOLDER, "/tv/Janosik DVD", "Janosik S01E07E08E09", "VIDEO_TS", "VTS_01_1.VOB").toAbsolutePath()));
    ep.addToMediaFiles(new MediaFile(Paths.get(FOLDER, "/tv/Janosik DVD", "Janosik S01E07E08E09", "VIDEO_TS-thumb.jpg").toAbsolutePath()));
    disc.addEpisode(ep);

    discEP.setTitle("DVDEpisodeInRoot");
    discEP.setYear("2009");
    discEP.setPath(FOLDER + "/tv/DVDEpisodeInRoot");
    ep = new TvShowEpisode();
    ep.setPath(FOLDER + "/tv/DVDEpisodeInRoot/S01EP01 title");
    ep.setTvShow(discEP);
    ep.setDisc(true);
    ep.setTitle("disc ep");
    ep.setAiredSeason(1);
    ep.setAiredEpisode(1);
    ep.addToMediaFiles(new MediaFile(Paths.get(FOLDER, "/tv/DVDEpisodeInRoot", "S01EP01 title", "VTS_01_1.VOB").toAbsolutePath()));
    discEP.addEpisode(ep);
  }

  @Test
  public void tvRenamerPatterns() {
    // SINGLE - RECOMMENDED
    assertEqual(p("/singleshow (2009)/Season 1/singleshow - S01E02 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual(p("/singleshow (2009)/Season 1/E02 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual(p("/singleshow (2009)/Season 1/S01E02 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual(p("/singleshow (2009)/Season 1/1x04 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual(p("/singleshow (2009)/102 - singleEP.avi"), gen(single, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual(p("/singleshow (2009)/1x04 - singleEP.avi"), gen(single, "$N ($Y)", "", "$1x$D - $T", true));

    // SINGLE - not recommended, but working
    assertEqual(p("/singleshow (2009)/Season 1/S01 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual(p("/singleshow (2009)/E02 - singleEP.avi"), gen(single, "$N ($Y)", "", "E$E - $T", false));
    assertEqual(p("/singleshow (2009)/E02.avi"), gen(single, "$N ($Y)", "", "E$E", false));
    assertEqual(p("/singleshow (2009)/Season 01/102 303- singleEP.avi"), gen(single, "$N ($Y)", "Season $2", "$1$E $3$4- $T", false));
    assertEqual(p("/singleshow (2009)/Season 01/102 3x04- singleEP.avi"), gen(single, "$N ($Y)", "Season $2", "$1$E $3x$D- $T", false));
    assertEqual(p("/singleshow (2009)/singleEP.avi"), gen(single, "$N ($Y)", "", "$T", false));
    assertEqual(p("/singleshow (2009)/singleEPsingleEP.avi"), gen(single, "$N ($Y)", "", "$T$T", false));
    assertEqual(p("/singleshow (2009)/singleshow - S101E02 - singleEP.avi"), gen(single, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual(p("/singleshow (2009)/singleshow - S1E0204 - singleEP.avi"), gen(single, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

    // *******************
    // COPY 1:1 FROM ABOVE
    // *******************

    // MULTI - RECOMMENDED
    assertEqual(p("/multishow (2009)/Season 1/multishow - S01E02 S01E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual(p("/multishow (2009)/Season 1/E02 E03 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual(p("/multishow (2009)/Season 1/S01E02 S01E03 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual(p("/multishow (2009)/Season 1/1x04 1x05 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual(p("/multishow (2009)/102 103 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual(p("/multishow (2009)/1x04 1x05 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$1x$D - $T", true));

    // MULTI - not recommended, but working
    assertEqual(p("/multishow (2009)/Season 1/S01 S01 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual(p("/multishow (2009)/E02 E03 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "E$E - $T", false));
    assertEqual(p("/multishow (2009)/E02 E03.avi"), gen(multi, "$N ($Y)", "", "E$E", false));
    assertEqual(p("/multishow (2009)/Season 01/102 103 303 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $2", "$1$E $3$4 - $T", false));
    assertEqual(p("/multishow (2009)/Season 01/102 103 3x04 - multiEP2 - multiEP3.avi"),
        gen(multi, "$N ($Y)", "Season $2", "$1$E $3x$D - $T", false));
    assertEqual(p("/multishow (2009)/multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$T", false));
    assertEqual(p("/multishow (2009)/multiEP2 - multiEP3 multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$T$T", false));
    assertEqual(p("/multishow (2009)/multishow - S101E02 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual(p("/multishow (2009)/multishow - S1E02 S1E0304 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double
  }

  @Test
  public void testDiscEpisode() throws IOException {
    Utils.copyDirectoryRecursive(Paths.get("target/test-classes/testtvshows"), Paths.get(FOLDER, "tv"));
    TvShowRenamer.renameEpisode(discEP.getEpisode(1, 1));
    TvShowRenamer.renameEpisode(disc.getEpisode(1, 2));
  }

  private Path gen(TvShow show, String showPattern, String seasonPattern, String filePattern, boolean recommended) {
    Assert.assertEquals(recommended, TvShowRenamer.isRecommended(seasonPattern, filePattern));
    String sh = TvShowRenamer.getTvShowFoldername(showPattern, show);
    String se = TvShowRenamer.getSeasonFoldername(seasonPattern, show, show.getEpisodes().get(0).getSeason());
    String ep = TvShowRenamer.generateEpisodeFilenames(filePattern, show, show.getEpisodesMediaFiles().get(0)).get(0).getFilename();
    System.out.println(new File(sh, se + File.separator + ep).toString());
    // return new File(sh, se + File.separator + ep).toString();
    return Paths.get(sh, se, ep);
  }

  /**
   * string to path for unix/linux comparison
   *
   * @param path
   * @return
   */
  private Path p(String path) {
    return Paths.get(path);
  }

  @Test
  public void testRename() {
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");

    testSimpleEpisode();
    testMultiEpisode();
    testPartedEpisode();
    testComplexEpisode();
  }

  /**
   * just a test of a simple episode (one EP file with some extra files)
   */
  private void testSimpleEpisode() {
    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/simple");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_simple/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear("2008");
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    TvShowRenamer.renameTvShowRoot(show);
    TvShowRenamer.renameEpisode(ep);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.deu.srt");
    assertThat(sub).exists();
  }

  /**
   * multi episode file test
   */
  private void testMultiEpisode() {
    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/multi");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_multi/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear("2008");
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // multi episode file
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTitle("Pilot 2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(2);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    TvShowRenamer.renameTvShowRoot(show);
    TvShowRenamer.renameEpisode(ep);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.deu.srt");
    assertThat(sub).exists();
  }

  /**
   * just a test of a parted episode (two EP files with some extra files)
   */
  private void testPartedEpisode() {
    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/parted");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_parted/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear("2008");
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.part1.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.part2.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    ep.reEvaluateStacking();
    show.addEpisode(ep);

    TvShowRenamer.renameTvShowRoot(show);
    TvShowRenamer.renameEpisode(ep);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.part1.mkv");
    assertThat(video).exists();
    video = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.part2.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.deu.srt");
    assertThat(sub).exists();
  }

  /**
   * this is a really sick test: a parted multi episode (two EP files containing two EPs with some extra files)
   */
  private void testComplexEpisode() {
    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/complex");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_complex/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear("2008");
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part1.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part2.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    ep.reEvaluateStacking();
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTitle("Pilot 2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part1.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part2.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    ep.reEvaluateStacking();
    show.addEpisode(ep);

    TvShowRenamer.renameTvShowRoot(show);
    TvShowRenamer.renameEpisode(ep);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.part1.mkv");
    assertThat(video).exists();
    video = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.part2.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.deu.srt");
    assertThat(sub).exists();
  }
}
