/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.panels.episode;

import static org.tinymediamanager.core.Constants.SEASON_POSTER;
import static org.tinymediamanager.core.Constants.THUMB;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ImageLabel.Position;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.converter.MediaInfoAudioCodecConverter;
import org.tinymediamanager.ui.converter.MediaInfoVideoCodecConverter;
import org.tinymediamanager.ui.converter.MediaInfoVideoFormatConverter;
import org.tinymediamanager.ui.panels.MediaInformationLogosPanel;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEpisodeInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeInformationPanel extends JPanel {
  private static final long           serialVersionUID = 2032708149757390567L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** UI components */
  private JPanel                      panelTop;
  private StarRater                   panelRatingStars;
  private JLabel                      lblTvShowName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private JLabel                      lblEpisodeTitle;
  private ImageLabel                  lblEpisodeThumb;
  private ImageLabel                  lblSeasonPoster;
  private JTextPane                   tpOverview;
  private MediaInformationLogosPanel  panelLogos;
  private JPanel                      panelDetails;
  private JLabel                      lblMediaLogoResolution;
  private JLabel                      lblMediaLogoVideoCodec;
  private JLabel                      lblMediaLogoAudio;

  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;
  private JPanel                      panelLeft;
  private JLabel                      lblSeasonPosterSize;
  private JLabel                      lblEpisodeThumbSize;
  private JSeparator                  separator;
  private JLabel                      lblPlot;
  private JSeparator                  separator_1;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowEpisodeSelectionModel
   *          the tv show selection model
   */
  public TvShowEpisodeInformationPanel(TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel) {
    this.tvShowEpisodeSelectionModel = tvShowEpisodeSelectionModel;
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("70dlu:grow"), FormSpecs.UNRELATED_GAP_COLSPEC,
            ColumnSpec.decode("200dlu:grow(2)"), FormSpecs.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    panelLeft = new JPanel();
    panelLeft.setLayout(new ColumnLayout());
    add(panelLeft, "2, 2, fill, fill");

    lblSeasonPoster = new ImageLabel(false, false, true);
    lblSeasonPoster.setDesiredAspectRatio(2 / 3.0f);
    panelLeft.add(lblSeasonPoster);
    lblSeasonPoster.setPosition(Position.BOTTOM_LEFT);
    lblSeasonPoster.enableLightbox();

    lblSeasonPosterSize = new JLabel(BUNDLE.getString("mediafiletype.season_poster")); //$NON-NLS-1$
    panelLeft.add(lblSeasonPosterSize);
    panelLeft.add(Box.createVerticalStrut(20));

    lblEpisodeThumb = new ImageLabel(false, false, true);
    lblEpisodeThumb.setDesiredAspectRatio(16 / 9.0f);
    panelLeft.add(lblEpisodeThumb);
    lblEpisodeThumb.setPosition(Position.BOTTOM_LEFT);
    lblEpisodeThumb.enableLightbox();

    lblEpisodeThumbSize = new JLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
    panelLeft.add(lblEpisodeThumbSize);

    panelTop = new JPanel();
    panelTop.setBorder(null);
    add(panelTop, "4, 2, fill, fill");
    panelTop.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow"), FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.DEFAULT_COLSPEC, },
        new RowSpec[] { RowSpec.decode("fill:default"), FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, RowSpec.decode("top:50px:grow(2)"), }));

    JPanel panelTvShowHeader = new JPanel();
    panelTop.add(panelTvShowHeader, "2, 1, 3, 1, fill, top");
    panelTvShowHeader.setBorder(null);
    panelTvShowHeader.setLayout(new BorderLayout(0, 0));

    JPanel panelMovieTitle = new JPanel();
    panelTvShowHeader.add(panelMovieTitle, BorderLayout.NORTH);
    panelMovieTitle.setLayout(new BorderLayout(0, 0));
    lblTvShowName = new JLabel("");
    panelMovieTitle.add(lblTvShowName);
    TmmFontHelper.changeFont(lblTvShowName, 1.33, Font.BOLD);

    lblEpisodeTitle = new JLabel();
    panelTop.add(lblEpisodeTitle, "2, 2");

    separator = new JSeparator();
    panelTop.add(separator, "2, 4, 3, 1");

    panelDetails = new TvShowEpisodeDetailsPanel(tvShowEpisodeSelectionModel);
    panelTop.add(panelDetails, "2, 6, 3, 1");

    separator_1 = new JSeparator();
    panelTop.add(separator_1, "2, 8");

    JPanel panelRatingTagline = new JPanel();
    panelTop.add(panelRatingTagline, "2, 10");
    panelRatingTagline
        .setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.DEFAULT_COLSPEC, ColumnSpec.decode("default:grow"), },
            new RowSpec[] { RowSpec.decode("24px"), }));

    lblRating = new JLabel("");
    panelRatingTagline.add(lblRating, "2, 1, left, center");

    lblVoteCount = new JLabel("");
    panelRatingTagline.add(lblVoteCount, "3, 1, left, center");

    panelRatingStars = new StarRater(10, 1);
    panelRatingTagline.add(panelRatingStars, "1, 1, left, top");
    panelRatingStars.setEnabled(false);

    panelTop.add(new JSeparator(), "2, 12, 3, 1");

    panelLogos = new MediaInformationLogosPanel();
    panelTop.add(panelLogos, "2, 14, 3, 1, left, default");

    panelTop.add(new JSeparator(), "2, 16, 3, 1");

    lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    lblPlot.setFont(lblPlot.getFont().deriveFont(Font.BOLD));
    panelTop.add(lblPlot, "2, 18");

    JScrollPane scrollPaneOverview = new JScrollPane();
    scrollPaneOverview.setBorder(null);
    tpOverview = new JTextPane();
    tpOverview.setOpaque(false);
    tpOverview.setEditable(false);
    scrollPaneOverview.setViewportView(tpOverview);

    JPanel panelOverview = new JPanel();
    panelTop.add(panelOverview, "2, 19, 3, 1, fill, fill");
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    // beansbinding init
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a movie
        if (source instanceof TvShowEpisodeSelectionModel) {
          TvShowEpisodeSelectionModel model = (TvShowEpisodeSelectionModel) source;
          setSeasonPoster(model.getSelectedTvShowEpisode());
          setEpisodeThumb(model.getSelectedTvShowEpisode());
          panelLogos.setMediaInformationSource(model.getSelectedTvShowEpisode());
        }
        if ((source.getClass() == TvShowEpisode.class && THUMB.equals(property))) {
          TvShowEpisode episode = (TvShowEpisode) source;
          setEpisodeThumb(episode);
        }
        if ((source.getClass() == TvShowEpisode.class && SEASON_POSTER.equals(property))) {
          TvShowEpisode episode = (TvShowEpisode) source;
          setSeasonPoster(episode);
        }
      }
    };

    this.tvShowEpisodeSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void setSeasonPoster(TvShowEpisode tvShowEpisode) {
    lblSeasonPoster.clearImage();
    lblSeasonPoster.setImagePath(tvShowEpisode.getTvShowSeason().getPoster());
    Dimension posterSize = tvShowEpisode.getTvShowSeason().getPosterSize();
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblSeasonPosterSize.setText(BUNDLE.getString("mediafiletype.season_poster") + " - " + posterSize.width + "x" + posterSize.height); //$NON-NLS-1$
    }
    else {
      lblSeasonPosterSize.setText(BUNDLE.getString("mediafiletype.season_poster")); //$NON-NLS-1$
    }
  }

  private void setEpisodeThumb(TvShowEpisode tvShowEpisode) {
    lblEpisodeThumb.clearImage();
    lblEpisodeThumb.setImagePath(tvShowEpisode.getArtworkFilename(MediaFileType.THUMB));
    Dimension thumbSize = tvShowEpisode.getArtworkDimension(MediaFileType.THUMB);
    if (thumbSize.width > 0 && thumbSize.height > 0) {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb") + " - " + thumbSize.width + "x" + thumbSize.height); //$NON-NLS-1$
    }
    else {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty = BeanProperty
        .create("selectedTvShowEpisode.tvShow.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty, lblTvShowName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_1 = BeanProperty
        .create("selectedTvShowEpisode.titleForUi");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_1, lblEpisodeTitle, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_3 = BeanProperty.create("selectedTvShowEpisode.plot");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JTextPane, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_3, tpOverview, jTextPaneBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Float> tvShowEpisodeSelectionModelBeanProperty_4 = BeanProperty.create("selectedTvShowEpisode.rating");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<TvShowEpisodeSelectionModel, Float, StarRater, Float> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, panelRatingStars, starRaterBeanProperty);
    autoBinding_4.bind();
    //
    AutoBinding<TvShowEpisodeSelectionModel, Float, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, lblRating, jLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_6 = BeanProperty
        .create("selectedTvShowEpisode.mediaInfoVideoFormat");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_1 = BeanProperty.create("icon");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, Icon> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_6, lblMediaLogoResolution, jLabelBeanProperty_1);
    autoBinding_7.setConverter(new MediaInfoVideoFormatConverter());
    autoBinding_7.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_7 = BeanProperty
        .create("selectedTvShowEpisode.mediaInfoVideoCodec");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, Icon> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_7, lblMediaLogoVideoCodec, jLabelBeanProperty_1);
    autoBinding_8.setConverter(new MediaInfoVideoCodecConverter());
    autoBinding_8.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_8 = BeanProperty
        .create("selectedTvShowEpisode.mediaInfoAudioCodecAndChannels");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, Icon> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_8, lblMediaLogoAudio, jLabelBeanProperty_1);
    autoBinding_9.setConverter(new MediaInfoAudioCodecConverter());
    autoBinding_9.bind();
  }
}
