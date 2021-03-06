/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.parameters.dialogs;

import java.text.NumberFormat;
import com.google.common.collect.Range;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.visualization.chromatogram.TICPlot;
import io.github.mzmine.modules.visualization.chromatogram.TICPlotType;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.ranges.DoubleRangeComponent;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

/**
 * This class extends ParameterSetupDialog class, including a TICPlot. This is used to preview how
 * the selected raw data filters work.
 *
 * Slightly modified to add the possibility of switching to TIC (versus Base Peak) preview.
 */
public abstract class ParameterSetupDialogWithChromatogramPreview extends ParameterSetupDialog {


  private RawDataFile[] dataFiles;
  private RawDataFile previewDataFile;

  // Dialog components
  private BorderPane pnlPreviewFields;
  private ComboBox<RawDataFile> comboDataFileName;
  private DoubleRangeComponent rtRangeBox, mzRangeBox;
  private CheckBox previewCheckBox;

  // Show as TIC
  private ComboBox<TICPlotType> ticViewComboBox;

  // XYPlot
  private TICPlot ticPlot;

  public ParameterSetupDialogWithChromatogramPreview(boolean valueCheckRequired,
      ParameterSet parameters) {
    super(valueCheckRequired, parameters);

    dataFiles = MZmineCore.getProjectManager().getCurrentProject().getDataFiles();

    if (dataFiles.length == 0)
      return;

    RawDataFile selectedFiles[] = MZmineCore.getDesktop().getSelectedDataFiles();

    if (selectedFiles.length > 0)
      previewDataFile = selectedFiles[0];
    else
      previewDataFile = dataFiles[0];

    previewCheckBox = new CheckBox("Show preview");
    previewCheckBox.setOnAction(e -> {
      if (previewCheckBox.isSelected()) {
        showPreview();
      } else {
        hidePreview();
      }
    });
    // previewCheckBox.setHorizontalAlignment(SwingConstants.CENTER);

    paramsPane.add(new Separator(), 0, getNumberOfParameters() + 1);
    paramsPane.add(previewCheckBox, 0, getNumberOfParameters() + 2);

    // Elements of pnlLab
    FlowPane pnlLab = new FlowPane(Orientation.VERTICAL);
    // pnlLab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // pnlLab.getChildren().add(Box.createVerticalStrut(5));
    pnlLab.getChildren().add(new Label("Data file "));
    // pnlLab.add(Box.createVerticalStrut(20));
    pnlLab.getChildren().add(new Label("Plot Type "));
    // pnlLab.add(Box.createVerticalStrut(25));
    pnlLab.getChildren().add(new Label("RT range "));
    // pnlLab.add(Box.createVerticalStrut(15));
    pnlLab.getChildren().add(new Label("m/z range "));

    // Elements of pnlFlds
    FlowPane pnlFlds = new FlowPane(Orientation.VERTICAL);

    // pnlFlds.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    comboDataFileName = new ComboBox<RawDataFile>(
        MZmineCore.getProjectManager().getCurrentProject().getRawDataFiles());
    comboDataFileName.getSelectionModel().select(previewDataFile);
    comboDataFileName.setOnAction(e -> {
      int ind = comboDataFileName.getSelectionModel().getSelectedIndex();
      if (ind >= 0) {
        previewDataFile = dataFiles[ind];
        parametersChanged();
      }
    });

    ticViewComboBox =
        new ComboBox<TICPlotType>(FXCollections.observableArrayList(TICPlotType.values()));
    ticViewComboBox.getSelectionModel().select(TICPlotType.TIC);
    ticViewComboBox.setOnAction(e -> parametersChanged());

    rtRangeBox = new DoubleRangeComponent(MZmineCore.getConfiguration().getRTFormat());
    rtRangeBox.setValue(previewDataFile.getDataRTRange(1));

    mzRangeBox = new DoubleRangeComponent(MZmineCore.getConfiguration().getMZFormat());
    mzRangeBox.setValue(previewDataFile.getDataMZRange(1));

    pnlFlds.getChildren().add(comboDataFileName);
    // pnlFlds.add(Box.createVerticalStrut(10));
    pnlFlds.getChildren().add(ticViewComboBox);
    // pnlFlds.add(Box.createVerticalStrut(20));
    pnlFlds.getChildren().add(rtRangeBox);
    // pnlFlds.add(Box.createVerticalStrut(5));
    pnlFlds.getChildren().add(mzRangeBox);

    // Put all together
    pnlPreviewFields = new BorderPane();

    pnlPreviewFields.setLeft(pnlLab);
    pnlPreviewFields.setCenter(pnlFlds);
    pnlPreviewFields.setVisible(false);

    ticPlot = new TICPlot();
    // ticPlot.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    // ticPlot.setMinimumSize(new Dimension(400, 300));

    paramsPane.add(pnlPreviewFields, 0, getNumberOfParameters() + 3);



  }

  /**
   * Get the parameters related to the plot and call the function addRawDataFile() to add the data
   * file to the plot
   *
   * @param dataFile
   */
  protected abstract void loadPreview(TICPlot ticPlot, RawDataFile dataFile, Range<Double> rtRange,
      Range<Double> mzRange);

  private void updateTitle() {

    NumberFormat rtFormat = MZmineCore.getConfiguration().getRTFormat();
    NumberFormat mzFormat = MZmineCore.getConfiguration().getMZFormat();

    Range<Double> rtRange = rtRangeBox.getValue();
    Range<Double> mzRange = mzRangeBox.getValue();

    String title = "m/z: " + mzFormat.format(mzRange.lowerEndpoint()) + " - "
        + mzFormat.format(mzRange.upperEndpoint()) + ", RT: "
        + rtFormat.format(rtRange.lowerEndpoint()) + " - "
        + rtFormat.format(rtRange.upperEndpoint());

    // update plot title
    ticPlot.setTitle(previewDataFile.getName(), title);
  }



  public void showPreview() {
    // Set the height of the preview to 200 cells, so it will span
    // the whole vertical length of the dialog (buttons are at row
    // no 100). Also, we set the weight to 10, so the preview
    // component will consume most of the extra available space.
    paramsPane.add(ticPlot, 3, 0, 1, 200);
    pnlPreviewFields.setVisible(true);
    // updateMinimumSize();
    // pack();
    parametersChanged();
    // previewCheckBox.setSelected(true);
  }

  public void hidePreview() {
    paramsPane.getChildren().remove(ticPlot);
    pnlPreviewFields.setVisible(false);
    // updateMinimumSize();
    // pack();
    previewCheckBox.setSelected(false);
  }

  public TICPlotType getPlotType() {
    return (ticViewComboBox.getSelectionModel().getSelectedItem());
  }

  public void setPlotType(TICPlotType plotType) {
    ticViewComboBox.getSelectionModel().select(plotType);
  }

  public RawDataFile getPreviewDataFile() {
    return this.previewDataFile;
  }

  @Override
  protected void parametersChanged() {

    // Update preview as parameters have changed
    if ((previewCheckBox == null) || (!previewCheckBox.isSelected()))
      return;

    Range<Double> rtRange = rtRangeBox.getValue();
    Range<Double> mzRange = mzRangeBox.getValue();
    updateParameterSetFromComponents();

    loadPreview(ticPlot, previewDataFile, rtRange, mzRange);

    updateTitle();

  }

  public TICPlot getTicPlot() {
    return ticPlot;
  }

  public void setTicPlot(TICPlot ticPlot) {
    this.ticPlot = ticPlot;
  }

}
