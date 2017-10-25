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
package org.tinymediamanager.ui.movies.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a movie cast filter
 * 
 * @author Manuel Laggner
 */
public class MovieCastFilter extends AbstractMovieUIFilter {
  private JTextField textField;

  @Override
  public String getId() {
    return "movieCast";
  }

  @Override
  public String getFilterValueAsString() {
    return textField.getText();
  }

  @Override
  public void setFilterValue(Object value) {
    if (value != null && value instanceof String) {
      textField.setText((String) value);
    }
  }

  @Override
  public boolean accept(Movie movie) {
    String name = textField.getText();

    if (StringUtils.isBlank(name)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(name));

    // director
    for (Person director : movie.getDirectors()) {
      if (StringUtils.isNotEmpty(director.getName())) {
        Matcher matcher = pattern.matcher(director.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }

    // writer
    for (Person writer : movie.getWriters()) {
      if (StringUtils.isNotEmpty(writer.getName())) {
        Matcher matcher = pattern.matcher(writer.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }

    // actors
    for (Person cast : movie.getActors()) {
      if (StringUtils.isNotEmpty(cast.getName())) {
        Matcher matcher = pattern.matcher(cast.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }

    // producers
    for (Person producer : movie.getProducers()) {
      if (StringUtils.isNotEmpty(producer.getName())) {
        Matcher matcher = pattern.matcher(producer.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    textField = new JTextField();
    return textField;
  }
}
