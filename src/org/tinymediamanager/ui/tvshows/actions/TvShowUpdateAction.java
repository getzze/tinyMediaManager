/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmSwingWorker;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowUpdateAction. Update a single TV show rather than the whole data source
 * 
 * @author Manuel Laggner
 */
public class TvShowUpdateAction extends AbstractAction {
  private static final long           serialVersionUID = 7216738427209633666L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowUpdateAction() {
    putValue(NAME, BUNDLE.getString("tvshow.update")); //$NON-NLS-1$
    putValue(LARGE_ICON_KEY, IconManager.REFRESH);
    putValue(SMALL_ICON, IconManager.REFRESH);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    List<File> tvShowFolders = new ArrayList<File>();

    if (selectedTvShows.isEmpty()) {
      return;
    }

    for (TvShow tvShow : selectedTvShows) {
      tvShowFolders.add(new File(tvShow.getPath()));
    }

    @SuppressWarnings("rawtypes")
    TmmSwingWorker task = new TvShowUpdateDatasourceTask(tvShowFolders);
    if (!MainWindow.executeMainTask(task)) {
      JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
    }
  }
}
