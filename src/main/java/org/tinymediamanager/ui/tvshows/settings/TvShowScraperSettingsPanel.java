/*
 * Copyright 2012 - 2017 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.connector.TvShowConnectors;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.panels.MediaScraperConfigurationPanel;
import org.tinymediamanager.ui.panels.ScrollablePanel;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowScraperSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperSettingsPanel extends ScrollablePanel {
  private static final long                    serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());             //$NON-NLS-1$

  private TvShowSettings                       settings         = TvShowModuleManager.SETTINGS;
  private List<TvShowScraper>                  scrapers         = ObservableCollections.observableList(new ArrayList<TvShowScraper>());

  /** UI components */
  private JComboBox                            cbScraperLanguage;
  private JComboBox                            cbCertificationCountry;
  private JCheckBox                            chckbxAutomaticallyScrapeImages;
  private TmmTable                             tableScraper;
  private JTextPane                            tpScraperDescription;
  private JPanel                               panelScraperOptions;
  private JComboBox<TvShowConnectors>          cbNfoFormat;
  private JComboBox<CertificationStyleWrapper> cbCertificationStyle;

  private ItemListener                         comboBoxListener;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  public TvShowScraperSettingsPanel() {
    comboBoxListener = e -> checkChanges();

    // pre-init
    MediaScraper defaultMediaScraper = TvShowList.getInstance().getDefaultMediaScraper();
    int selectedIndex = 0;
    int counter = 0;
    for (MediaScraper scraper : TvShowList.getInstance().getAvailableMediaScrapers()) {
      TvShowScraper tvShowScraper = new TvShowScraper(scraper);
      if (scraper.equals(defaultMediaScraper)) {
        tvShowScraper.defaultScraper = true;
        selectedIndex = counter;
      }
      scrapers.add(tvShowScraper);
      counter++;
    }

    // UI init
    initComponents();
    initDataBindings();

    // data init

    // add a CSS rule to force body tags to use the default label font
    // instead of the value in javax.swing.text.html.default.csss
    Font font = UIManager.getFont("Label.font");
    Color color = UIManager.getColor("Label.foreground");
    String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: rgb(" + color.getRed() + ","
        + color.getGreen() + "," + color.getBlue() + "); }";
    ((HTMLDocument) tpScraperDescription.getDocument()).getStyleSheet().addRule(bodyRule);

    // adjust table columns
    // Checkbox and Logo shall have minimal width
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 0, 2);
    TableColumnResizer.setMaxWidthForColumn(tableScraper, 1, 2);
    TableColumnResizer.adjustColumnPreferredWidths(tableScraper, 5);

    // implement listener to simulate button group
    tableScraper.getModel().addTableModelListener(arg0 -> {
      // click on the checkbox
      if (arg0.getColumn() == 0) {
        int row = arg0.getFirstRow();
        TvShowScraper changedScraper = scrapers.get(row);
        // if flag inNFO was changed, change all other trailers flags
        if (changedScraper.getDefaultScraper()) {
          settings.setScraper(changedScraper.getScraperId());
          for (TvShowScraper scraper : scrapers) {
            if (scraper != changedScraper) {
              scraper.setDefaultScraper(Boolean.FALSE);
            }
          }
        }
      }
    });

    // implement selection listener to load settings
    tableScraper.getSelectionModel().addListSelectionListener(e -> {
      int index = tableScraper.convertRowIndexToModel(tableScraper.getSelectedRow());
      if (index > -1) {
        panelScraperOptions.removeAll();
        if (scrapers.get(index).getMediaProvider().getProviderInfo().getConfig().hasConfig()) {
          panelScraperOptions.add(new MediaScraperConfigurationPanel(scrapers.get(index).getMediaProvider()));
        }
        panelScraperOptions.revalidate();
      }
    });

    // select default TV show scraper
    if (counter > 0) {
      tableScraper.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
    }

    buildComboBoxes();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][][][500lp,grow]", "[][200lp][][][20lp][][][][20lp][][][20lp][][]"));
    {
      JLabel lblMetadataScraper = new JLabel(BUNDLE.getString("scraper.metadata")); // $NON-NLS-1$
      TmmFontHelper.changeFont(lblMetadataScraper, 1.16667, Font.BOLD);
      add(lblMetadataScraper, "cell 0 0 4 1");
    }
    {
      tableScraper = new TmmTable();
      tableScraper.setRowHeight(29);

      JScrollPane scrollPaneScraper = new JScrollPane(tableScraper);
      tableScraper.configureScrollPane(scrollPaneScraper);
      add(scrollPaneScraper, "cell 1 1 2 1,grow");
    }
    {
      JScrollPane scrollPaneScraperDetails = new JScrollPane();
      add(scrollPaneScraperDetails, "cell 3 1,grow");
      scrollPaneScraperDetails.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPaneScraperDetails.setBorder(null);

      JPanel panelScraperDetails = new ScrollablePanel();
      scrollPaneScraperDetails.setViewportView(panelScraperDetails);
      panelScraperDetails.setLayout(new MigLayout("", "[grow]", "[][]"));

      tpScraperDescription = new JTextPane();
      tpScraperDescription.setOpaque(false);
      tpScraperDescription.setEditorKit(new HTMLEditorKit());
      panelScraperDetails.add(tpScraperDescription, "cell 0 0,grow");

      panelScraperOptions = new JPanel();
      panelScraperOptions.setLayout(new FlowLayout(FlowLayout.LEFT));
      panelScraperDetails.add(panelScraperOptions, "cell 0 1,growx,aligny top");
    }
    {
      JLabel lblScraperLanguage = new JLabel(BUNDLE.getString("Settings.preferredLanguage")); // $NON-NLS-1$
      add(lblScraperLanguage, "cell 1 2,growx");

      cbScraperLanguage = new JComboBox(MediaLanguages.values());
      add(cbScraperLanguage, "cell 2 2");

      JLabel lblCountry = new JLabel(BUNDLE.getString("Settings.certificationCountry")); // $NON-NLS-1$
      add(lblCountry, "cell 1 3");

      cbCertificationCountry = new JComboBox(CountryCode.values());
      add(cbCertificationCountry, "cell 2 3");
    }
    {
      JLabel lblNfoSettingsT = new JLabel(BUNDLE.getString("Settings.nfo")); //$NON-NLS-1$
      add(lblNfoSettingsT, "cell 0 5 3 1");

      JLabel lblNfoFormatT = new JLabel(BUNDLE.getString("Settings.nfoFormat")); //$NON-NLS-1$
      add(lblNfoFormatT, "flowx,cell 1 6 3 1");

      cbNfoFormat = new JComboBox(TvShowConnectors.values());
      add(cbNfoFormat, "cell 1 6");

      JLabel lblCertificationFormatT = new JLabel(BUNDLE.getString("Settings.certificationformat")); //$NON-NLS-1$
      add(lblCertificationFormatT, "flowx,cell 1 7 3 1");

      cbCertificationStyle = new JComboBox();
      add(cbCertificationStyle, "cell 1 7");
    }
    {
      final JLabel lblScraperOptionsT = new JLabel(BUNDLE.getString("scraper.metadata.defaults")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperOptionsT, 1.16667, Font.BOLD);
      add(lblScraperOptionsT, "cell 0 9 4 1");
    }
    {
      final TvShowScraperMetadataPanel scraperMetadataPanel = new TvShowScraperMetadataPanel(settings.getScraperMetadataConfig());
      add(scraperMetadataPanel, "cell 1 10 3 1,grow");
    }
    {
      final JLabel lblArtworkScrapeT = new JLabel(BUNDLE.getString("Settings.images")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblArtworkScrapeT, 1.16667, Font.BOLD);
      add(lblArtworkScrapeT, "cell 0 12 3 1");
    }
    {
      chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape")); //$NON-NLS-1$
      add(chckbxAutomaticallyScrapeImages, "cell 1 13 3 1");
    }
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    CertificationStyleWrapper wrapper = (CertificationStyleWrapper) cbCertificationStyle.getSelectedItem();
    if (wrapper != null && settings.getCertificationStyle() != wrapper.style) {
      settings.setCertificationStyle(wrapper.style);
    }
  }

  private void buildComboBoxes() {
    cbCertificationStyle.removeItemListener(comboBoxListener);
    cbCertificationStyle.removeAllItems();

    // certification examples
    for (CertificationStyle style : CertificationStyle.values()) {
      CertificationStyleWrapper wrapper = new CertificationStyleWrapper();
      wrapper.style = style;
      cbCertificationStyle.addItem(wrapper);
      if (style == settings.getCertificationStyle()) {
        cbCertificationStyle.setSelectedItem(wrapper);
      }
    }

    cbCertificationStyle.addItemListener(comboBoxListener);
  }

  /*****************************************************************************************************
   * helper classes
   ****************************************************************************************************/
  public static class TvShowScraper extends AbstractModelObject {
    private MediaScraper scraper;
    private Icon         scraperLogo;
    private boolean      defaultScraper;

    public TvShowScraper(MediaScraper scraper) {
      this.scraper = scraper;
      if (scraper.getMediaProvider() == null || scraper.getMediaProvider().getProviderInfo() == null
          || scraper.getMediaProvider().getProviderInfo().getProviderLogo() == null) {
        scraperLogo = new ImageIcon();
      }
      else {
        scraperLogo = getScaledIcon(new ImageIcon(scraper.getMediaProvider().getProviderInfo().getProviderLogo()));
      }
    }

    private ImageIcon getScaledIcon(ImageIcon original) {
      Canvas c = new Canvas();
      FontMetrics fm = c.getFontMetrics(new JPanel().getFont());

      int height = (int) (fm.getHeight() * 2f);
      int width = original.getIconWidth() / original.getIconHeight() * height;

      BufferedImage scaledImage = Scalr.resize(ImageCache.createImage(original.getImage()), Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, width, height,
          Scalr.OP_ANTIALIAS);
      return new ImageIcon(scaledImage);
    }

    public String getScraperId() {
      return scraper.getId();
    }

    public String getScraperName() {
      return scraper.getName() + " - " + scraper.getVersion();
    }

    public String getScraperDescription() {
      // first try to get the localized version
      String description = null;
      try {
        description = BUNDLE.getString("scraper." + scraper.getId() + ".hint"); //$NON-NLS-1$
      }
      catch (Exception ignored) {
      }

      if (StringUtils.isBlank(description)) {
        // try to get a scraper text
        description = scraper.getDescription();
      }

      return description;
    }

    public Icon getScraperLogo() {
      return scraperLogo;
    }

    public Boolean getDefaultScraper() {
      return defaultScraper;
    }

    public void setDefaultScraper(Boolean newValue) {
      Boolean oldValue = this.defaultScraper;
      this.defaultScraper = newValue;
      firePropertyChange("defaultScraper", oldValue, newValue);
    }

    public IMediaProvider getMediaProvider() {
      return scraper.getMediaProvider();
    }
  }

  /*
   * helper for displaying the combobox with an example
   */
  private class CertificationStyleWrapper {
    private CertificationStyle style;

    @Override
    public String toString() {
      String bundleTag = BUNDLE.getString("Settings.certification." + style.name().toLowerCase());
      return bundleTag.replace("{}", CertificationStyle.formatCertification(Certification.DE_FSK16, style));
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, MediaLanguages> settingsBeanProperty_8 = BeanProperty.create("scraperLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, MediaLanguages, JComboBox, Object> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, cbScraperLanguage, jComboBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSettings, CountryCode> settingsBeanProperty_9 = BeanProperty.create("certificationCountry");
    AutoBinding<TvShowSettings, CountryCode, JComboBox, Object> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, cbCertificationCountry, jComboBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    JTableBinding<TvShowScraper, List<TvShowScraper>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, scrapers,
        tableScraper);
    //
    BeanProperty<TvShowScraper, Boolean> tvShowScraperBeanProperty = BeanProperty.create("defaultScraper");
    jTableBinding.addColumnBinding(tvShowScraperBeanProperty).setColumnName("Default").setColumnClass(Boolean.class);
    //
    BeanProperty<TvShowScraper, Icon> tvShowScraperBeanProperty_1 = BeanProperty.create("scraperLogo");
    jTableBinding.addColumnBinding(tvShowScraperBeanProperty_1).setColumnName("Logo").setColumnClass(Icon.class);
    //
    BeanProperty<TvShowScraper, String> tvShowScraperBeanProperty_2 = BeanProperty.create("scraperName");
    jTableBinding.addColumnBinding(tvShowScraperBeanProperty_2).setColumnName("Name").setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.scraperDescription");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, tableScraper, jTableBeanProperty,
        tpScraperDescription, jTextPaneBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSettings, TvShowConnectors> tvShowSettingsBeanProperty = BeanProperty.create("tvShowConnector");
    AutoBinding<TvShowSettings, TvShowConnectors, JComboBox, Object> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_1.bind();
  }
}
