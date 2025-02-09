package ch.xwr.seicentobilling.ui.desktop;

import java.util.Calendar;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.reports.ExportType;
import com.xdev.reports.tableexport.ui.TableExportPopup;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.XdevBeanContainer;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.util.NestedProperty;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.JasperManager;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.dal.CostAccountDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.CostAccount_;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Periode_;
import ch.xwr.seicentobilling.entities.ProjectLine;
import ch.xwr.seicentobilling.entities.ProjectLine_;
import ch.xwr.seicentobilling.entities.Project_;

public class ProjectLineTabView extends XdevView {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ProjectLineTabView.class);

	/**
	 *
	 */
	public ProjectLineTabView() {
		super();
		this.initUI();

		// Sort Tables
		this.tableLine.clear();
		final Object[] properties = { "prlReportDate", "project" };
		final boolean[] ordering = { false, true };
		this.tableLine.sort(properties, ordering);

		final Object[] properties2 = { "perYear", "perMonth", "costaccount" };
		final boolean[] ordering2 = { false, false, true };
		this.table.sort(properties2, ordering2);

		// set RO Fields
		setROFields();

		setDefaultFilter();
	}

	private void setDefaultFilter() {
		CostAccount bean = Seicento.getLoggedInCostAccount();
		if (bean == null) {
			bean = new CostAccountDAO().findAll().get(0);	//Dev Mode
		}

		final Calendar cal = Calendar.getInstance();
		final int iyear = cal.get(Calendar.YEAR);

		final Integer[] val = new Integer[]{iyear};
		final CostAccount[] val2 = new CostAccount[]{bean};
		final FilterData[] fd = new FilterData[]{new FilterData("perYear", new FilterOperator.Is(), val),
				new FilterData("costAccount", new FilterOperator.Is(), val2)};

		this.containerFilterComponent.setFilterData(fd);
	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdNewLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewLine_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null || this.table.getSelectedItem().getBean() == null ) {
			Notification.show("Neuen Zeile anlegen", "Keine Periode gewählt", Notification.Type.WARNING_MESSAGE);
			return;
		}

		final Long beanId = null;
		final Long objId = this.table.getSelectedItem().getBean().getPerId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupProjectLine();
	}

	private void popupProjectLine() {
		final Window win = ProjectLinePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadTableLineList();
			}
		});
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		reloadTableLineList();
		setROFields();

	}

	private void setROFields() {
		final boolean roFlag = isBooked();

		this.cmdNewLine.setEnabled(!roFlag);
		this.cmdUpdateLine.setEnabled(!roFlag);
		this.cmdDeleteLine.setEnabled(!roFlag);
		//this.cmdCopySingle.setEnabled(!roFlag);
		this.cmdUpdate.setEnabled(!roFlag);
		this.cmdDelete.setEnabled(!roFlag);
		this.cmdExcel.setEnabled(!roFlag);
		this.cmdCopyLine.setEnabled(!roFlag);

	}

	private boolean isBooked() {
		if (this.table.getSelectedItem() == null) {
			return false;
		}
		final Periode bean = this.table.getSelectedItem().getBean();
		if (LovState.BookingType.gebucht.equals(bean.getPerBookedExpense())) {
			return true;
		}
		return false;
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {

		UI.getCurrent().getSession().setAttribute("beanId",  null);
		UI.getCurrent().getSession().setAttribute("reason",  "new");

		popupPeriode();
	}


	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDelete}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDelete_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}

		ConfirmDialog.show(getUI(), "Datensatz löschen", "Wirklich löschen?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}

				if (retval.equals("cmdOk")) {
					doDelete();
				}
			}

			private void doDelete() {
				final Periode bean = ProjectLineTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPerId(), bean.getClass().getSimpleName());

				final PeriodeDAO dao = new PeriodeDAO();
				dao.remove(bean);

				ProjectLineTabView.this.table.removeItem(bean);
				ProjectLineTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					ProjectLineTabView.this.table.select(ProjectLineTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					// ignore
					LOG.error("Fehler beim Löschen", e);
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!",
						Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
		reloadMainTable();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			return;
		}
		final Periode bean = this.table.getSelectedItem().getBean();

		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getPerId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}


	private void reloadTableLineList() {
		if (this.table.getSelectedItem() == null) {
			return;
		}

		final Periode per = this.table.getSelectedItem().getBean();

		final XdevBeanContainer<ProjectLine> myCustomerList = this.tableLine.getBeanContainerDataSource();
		myCustomerList.removeAll();
		myCustomerList.addAll(new ProjectLineDAO().findByPeriode(per));

		if (per != null) {
			this.tableLine.refreshRowCache();
			this.tableLine.getBeanContainerDataSource().refresh();
		}
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReport_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Report starten", "Es wurde keine Zeile selektiert in der Tabelle", Notification.Type.WARNING_MESSAGE);
			return;
		}
		final Periode bean = this.table.getSelectedItem().getBean();

		final JasperManager jsp = new JasperManager();
		jsp.addParameter("Param_Periode", "" + bean.getPerId());
//		jsp.addParameter("Param_DateTo", sal.getSlrDate().toString());

		Page.getCurrent().open(jsp.getUri(JasperManager.ProjectLineReport1), "_blank");

	}

	private void popupExcelUpload() {
		final Window win = ExcelUploadPopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				reloadTableLineList();
			}
		});
		this.getUI().addWindow(win);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdUpdate}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdate_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.table.getSelectedItem().getBean().getPerId();
		UI.getCurrent().getSession().setAttribute("beanId",  beanId);
		UI.getCurrent().getSession().setAttribute("reason",  "update");

		popupPeriode();
	}

	private void popupPeriode() {
		final Window win = PeriodePopup.getPopupWindow();

		win.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				final String reason = (String) UI.getCurrent().getSession().getAttribute("reason");

				if (retval == null) {
					retval = "cmdCancel";
				}
				if (retval.equals("cmdSave")) {
					final Long beanId = (Long) UI.getCurrent().getSession().getAttribute("beanId");
					final Periode bean = new PeriodeDAO().find(beanId);
					//final Periode bean = new PeriodeDAO().getReference(beanId);
					//reloadMainTable();

					if ("new".equals(reason)) {
						ProjectLineTabView.this.table.addItem(bean);

					} else {
						reloadMainTable();
					}
				}

			}
		});
		this.getUI().addWindow(win);
	}

	private void reloadMainTable() {
		//save filter
		final FilterData[] fd = this.containerFilterComponent.getFilterData();
		this.containerFilterComponent.setFilterData(null);

		//clear+reload List
		this.table.removeAllItems();
		this.table.getBeanContainerDataSource().addAll(new PeriodeDAO().findAll());

		//this.table.refreshRowCache();

		//define sort
		final Object[] properties2 = { "perYear", "perMonth", "costaccount" };
		final boolean[] ordering2 = { false, false, true };
		this.table.sort(properties2, ordering2);

		//reassign filter
		this.containerFilterComponent.setFilterData(fd);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdDeleteLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteLine_buttonClick(final Button.ClickEvent event) {
		final XdevTable<ProjectLine> tab = this.tableLine;
		if (tab.getSelectedItem() == null) {
			Notification.show("Datensatz löschen", "Es wurde keine Zeile selektiert in der Tabelle",
					Notification.Type.WARNING_MESSAGE);
			return;
		}
		ConfirmDialog.show(getUI(), "Datensatz löschen", "Wirklich löschen?", new CloseListener() {
			@Override
			public void windowClose(final CloseEvent e) {
				String retval = UI.getCurrent().getSession().getAttribute(String.class);
				if (retval == null) {
					retval = "cmdCancel";
				}

				if (retval.equals("cmdOk")) {
					doDelete();
				}
			}

			private void doDelete() {
				final ProjectLine bean = tab.getSelectedItem().getBean();
				// Update RowObject
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPrlId(), bean.getClass().getSimpleName());
				// Delete Record
				final ProjectLineDAO dao = new ProjectLineDAO();
				dao.remove(bean);
				// refresh tab
				tab.removeItem(bean);
				tab.getBeanContainerDataSource().refresh();

				// if (!tab.isEmpty())
				// tab.select(tab.getCurrentPageFirstItemId());
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});

	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdUpdateLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final Long beanId = this.tableLine.getSelectedItem().getBean().getPrlId();
		final Long objId = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		popupProjectLine();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdExcel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdExcel_buttonClick(final Button.ClickEvent event) {
		if (this.table.getSelectedItem() == null) {
			Notification.show("Excel importieren", "Keine Periode gewählt", Notification.Type.WARNING_MESSAGE);
			return;
		}
		final Periode bean = this.table.getSelectedItem().getBean();

		UI.getCurrent().getSession().setAttribute("periodebean", bean);

		popupExcelUpload();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdCopyLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCopyLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final ProjectLine bean = this.tableLine.getSelectedItem().getBean();

		bean.setPrlId(new Long(0));
		bean.setPrlText(bean.getPrlText() + " (Kopie)");

		final ProjectLineDAO dao = new ProjectLineDAO();
		final ProjectLine newBean = dao.merge(bean);
		dao.save(newBean);

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getPrlId(), newBean.getClass().getSimpleName());

		reloadTableLineList();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdExport}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdExport_buttonClick(final Button.ClickEvent event) {
		TableExportPopup.show(this.tableLine, ExportType.XLSX, ExportType.PDF);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdInfoLine}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfoLine_buttonClick(final Button.ClickEvent event) {
		if (this.tableLine.getSelectedItem() == null) {
			return;
		}

		final ProjectLine bean = this.tableLine.getSelectedItem().getBean();
		final Window win = RowObjectView.getPopupWindow();

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getPrlId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #tableLine}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableLine_itemClick(final ItemClickEvent event) {
		// Notification.show("Event Triggered " + event.getButtonName(),
		// Notification.Type.TRAY_NOTIFICATION);

		if (event.isDoubleClick() && !isBooked()) {
			// Notification.show("Event Triggered ",
			// Notification.Type.TRAY_NOTIFICATION);
			final ProjectLine obj = (ProjectLine) event.getItemId();
			this.tableLine.select(obj); // reselect after double-click

			final Long beanId = obj.getPrlId(); // this.tableLine.getSelectedItem().getBean().getPrlId();
			final Long objId = null;

			UI.getCurrent().getSession().setAttribute("beanId", beanId);
			UI.getCurrent().getSession().setAttribute("objId", objId);

			popupProjectLine();
		}

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see FieldEvents.FocusListener#focus(FieldEvents.FocusEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_focus(final FieldEvents.FocusEvent event) {
		reloadMainTable();
	}

	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.horizontalSplitPanel = new XdevHorizontalSplitPanel();
		this.verticalLayout = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.actionLayout = new XdevHorizontalLayout();
		this.cmdNew = new XdevButton();
		this.cmdDelete = new XdevButton();
		this.cmdUpdate = new XdevButton();
		this.cmdReload = new XdevButton();
		this.cmdReport = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.table = new XdevTable<>();
		this.verticalLayoutRight = new XdevVerticalLayout();
		this.tabSheet = new XdevTabSheet();
		this.verticalLayoutReports = new XdevVerticalLayout();
		this.containerFilterComponent2 = new XdevContainerFilterComponent();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdNewLine = new XdevButton();
		this.cmdDeleteLine = new XdevButton();
		this.cmdUpdateLine = new XdevButton();
		this.cmdExcel = new XdevButton();
		this.cmdCopyLine = new XdevButton();
		this.cmdExport = new XdevButton();
		this.cmdInfoLine = new XdevButton();
		this.tableLine = new XdevTable<>();

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(40.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.containerFilterComponent.setImmediate(true);
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNew.setDescription(StringResourceUtils.optLocalizeString("{$cmdNew.description}", this));
		this.cmdNew.setTabIndex(1);
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDelete.setTabIndex(2);
		this.cmdUpdate.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/edit1.png"));
		this.cmdUpdate.setDescription("Periode bearbeiten...");
		this.cmdReload.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/reload2.png"));
		this.cmdReload.setDescription("Tabelle neu laden");
		this.cmdReload.setTabIndex(3);
		this.cmdReport.setIcon(
				new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/Printer_black_18.png"));
		this.cmdReport.setDescription("Jasper Report starten");
		this.cmdInfo
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.cmdInfo.setDescription("Objektinfo");
		this.cmdInfo.setTabIndex(4);
		this.table.setColumnReorderingAllowed(true);
		this.table.setTabIndex(5);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Periode.class, DAOs.get(PeriodeDAO.class).findAll(),
				NestedProperty.of(Periode_.costAccount, CostAccount_.csaName));
		this.table.setVisibleColumns(Periode_.perName.getName(), Periode_.perYear.getName(), Periode_.perMonth.getName(),
				Periode_.perBookedExpense.getName(), Periode_.perBookedProject.getName(), Periode_.perState.getName(),
				NestedProperty.path(Periode_.costAccount, CostAccount_.csaName));
		this.table.setColumnHeader("perName", "Periode");
		this.table.setColumnHeader("perYear", "Jahr");
		this.table.setConverter("perYear", ConverterBuilder.stringToInteger().build());
		this.table.setColumnCollapsed("perYear", true);
		this.table.setColumnHeader("perMonth", "Monat");
		this.table.setColumnCollapsed("perMonth", true);
		this.table.setColumnHeader("perBookedExpense", "Buchhaltung");
		this.table.setColumnHeader("perBookedProject", "Gebucht Projekt");
		this.table.setColumnCollapsed("perBookedProject", true);
		this.table.setColumnHeader("perState", "Status");
		this.table.setColumnCollapsed("perState", true);
		this.table.setColumnHeader("costAccount.csaName", "Kostenstelle");
		this.table.setColumnCollapsed("costAccount.csaName", true);
		this.verticalLayoutRight.setMargin(new MarginInfo(false));
		this.tabSheet.setTabIndex(6);
		this.tabSheet.setStyleName("framed");
		this.tabSheet.setImmediate(false);
		this.verticalLayoutReports.setMargin(new MarginInfo(false));
		this.horizontalLayout.setSpacing(false);
		this.horizontalLayout.setMargin(new MarginInfo(false));
		this.horizontalLayout.setImmediate(true);
		this.cmdNewLine
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNewLine.setCaption("Neu");
		this.cmdNewLine.setDescription("Rapportzeile hinzufügen");
		this.cmdNewLine.setClickShortcut(ShortcutAction.KeyCode.N, ShortcutAction.ModifierKey.CTRL);
		this.cmdDeleteLine
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdDeleteLine.setCaption("");
		this.cmdDeleteLine.setDescription("Rapportzeile löschen");
		this.cmdUpdateLine
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/edit1.png"));
		this.cmdUpdateLine.setCaption("");
		this.cmdUpdateLine.setDescription("Rapportzeile bearbeiten");
		this.cmdExcel.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/import24.png"));
		this.cmdExcel.setCaption("Import Excel");
		this.cmdExcel.setDescription("Rapporte aus Excel importieren");
		this.cmdCopyLine.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/copy1.png"));
		this.cmdCopyLine.setDescription("Einzelne Zeile kopieren");
		this.cmdExport
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/export24.png"));
		this.cmdExport.setCaption("Export");
		this.cmdExport.setDescription("Excel Export");
		this.cmdInfoLine
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.cmdInfoLine.setDescription("Objektinfo");
		this.tableLine.setColumnReorderingAllowed(true);
		this.tableLine.setColumnCollapsingAllowed(true);
		this.tableLine.setContainerDataSource(ProjectLine.class, false,
				NestedProperty.of(ProjectLine_.project, Project_.proName));
		this.tableLine.setVisibleColumns(ProjectLine_.prlReportDate.getName(), ProjectLine_.prlText.getName(),
				ProjectLine_.prlHours.getName(), ProjectLine_.prlRate.getName(), ProjectLine_.prlWorkType.getName(),
				NestedProperty.path(ProjectLine_.project, Project_.proName), ProjectLine_.project.getName(),
				ProjectLine_.prlState.getName());
		this.tableLine.setColumnHeader("prlReportDate", "Datum");
		this.tableLine.setConverter("prlReportDate", ConverterBuilder.stringToDate().dateOnly().build());
		this.tableLine.setColumnHeader("prlText", "Text");
		this.tableLine.setColumnHeader("prlHours", "Stunden");
		this.tableLine.setColumnAlignment("prlHours", Table.Align.RIGHT);
		this.tableLine.setConverter("prlHours",
				ConverterBuilder.stringToDouble().minimumFractionDigits(2).maximumFractionDigits(2).build());
		this.tableLine.setColumnHeader("prlRate", "Ansatz");
		this.tableLine.setColumnAlignment("prlRate", Table.Align.RIGHT);
		this.tableLine.setConverter("prlRate", ConverterBuilder.stringToDouble().currency().build());
		this.tableLine.setColumnHeader("prlWorkType", "Typ");
		this.tableLine.setColumnHeader("project.proName", "Projektname");
		this.tableLine.setColumnHeader("project", "Projekt");
		this.tableLine.setColumnCollapsed("project", true);
		this.tableLine.setColumnHeader("prlState", "Status");

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "perYear", "perMonth",
				"costAccount", "perBookedExpense", "perBookedProject", "perState");
		this.containerFilterComponent.setSearchableProperties("perName", "costAccount.csaName", "costAccount.csaCode");
		this.containerFilterComponent2.setContainer(this.tableLine.getBeanContainerDataSource(), "prlReportDate", "prlText",
				"prlHours", "prlRate", "prlWorkType", "project", "prlState");
		this.containerFilterComponent2.setSearchableProperties("prlText", "project.proName", "project.proExtReference");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdUpdate.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdUpdate);
		this.actionLayout.setComponentAlignment(this.cmdUpdate, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
		this.cmdReport.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReport);
		this.actionLayout.setComponentAlignment(this.cmdReport, Alignment.MIDDLE_CENTER);
		this.cmdInfo.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdInfo);
		this.actionLayout.setComponentAlignment(this.cmdInfo, Alignment.MIDDLE_CENTER);
		final CustomComponent actionLayout_spacer = new CustomComponent();
		actionLayout_spacer.setSizeFull();
		this.actionLayout.addComponent(actionLayout_spacer);
		this.actionLayout.setExpandRatio(actionLayout_spacer, 1.0F);
		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.actionLayout.setWidth(100, Unit.PERCENTAGE);
		this.actionLayout.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.actionLayout);
		this.verticalLayout.setComponentAlignment(this.actionLayout, Alignment.MIDDLE_CENTER);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.cmdNewLine.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdNewLine);
		this.horizontalLayout.setComponentAlignment(this.cmdNewLine, Alignment.MIDDLE_CENTER);
		this.cmdDeleteLine.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDeleteLine);
		this.horizontalLayout.setComponentAlignment(this.cmdDeleteLine, Alignment.MIDDLE_CENTER);
		this.cmdUpdateLine.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdUpdateLine);
		this.horizontalLayout.setComponentAlignment(this.cmdUpdateLine, Alignment.MIDDLE_CENTER);
		this.cmdExcel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdExcel);
		this.horizontalLayout.setComponentAlignment(this.cmdExcel, Alignment.MIDDLE_CENTER);
		this.cmdCopyLine.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCopyLine);
		this.horizontalLayout.setComponentAlignment(this.cmdCopyLine, Alignment.MIDDLE_CENTER);
		this.cmdExport.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdExport);
		this.horizontalLayout.setComponentAlignment(this.cmdExport, Alignment.MIDDLE_CENTER);
		this.cmdInfoLine.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdInfoLine);
		this.horizontalLayout.setComponentAlignment(this.cmdInfoLine, Alignment.MIDDLE_CENTER);
		this.containerFilterComponent2.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent2.setHeight(-1, Unit.PIXELS);
		this.verticalLayoutReports.addComponent(this.containerFilterComponent2);
		this.verticalLayoutReports.setComponentAlignment(this.containerFilterComponent2, Alignment.MIDDLE_CENTER);
		this.horizontalLayout.setSizeUndefined();
		this.verticalLayoutReports.addComponent(this.horizontalLayout);
		this.verticalLayoutReports.setComponentAlignment(this.horizontalLayout, Alignment.MIDDLE_LEFT);
		this.tableLine.setSizeFull();
		this.verticalLayoutReports.addComponent(this.tableLine);
		this.verticalLayoutReports.setComponentAlignment(this.tableLine, Alignment.MIDDLE_CENTER);
		this.verticalLayoutReports.setExpandRatio(this.tableLine, 100.0F);
		this.verticalLayoutReports.setSizeFull();
		this.tabSheet.addTab(this.verticalLayoutReports, "Rapportzeilen", null);
		this.tabSheet.setSelectedTab(this.verticalLayoutReports);
		this.tabSheet.setSizeFull();
		this.verticalLayoutRight.addComponent(this.tabSheet);
		this.verticalLayoutRight.setExpandRatio(this.tabSheet, 100.0F);
		this.verticalLayout.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayout);
		this.verticalLayoutRight.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.verticalLayoutRight);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdNew.addFocusListener(event -> this.cmdNew_focus(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdUpdate.addClickListener(event -> this.cmdUpdate_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdReport.addClickListener(event -> this.cmdReport_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.cmdNewLine.addClickListener(event -> this.cmdNewLine_buttonClick(event));
		this.cmdDeleteLine.addClickListener(event -> this.cmdDeleteLine_buttonClick(event));
		this.cmdUpdateLine.addClickListener(event -> this.cmdUpdateLine_buttonClick(event));
		this.cmdExcel.addClickListener(event -> this.cmdExcel_buttonClick(event));
		this.cmdCopyLine.addClickListener(event -> this.cmdCopyLine_buttonClick(event));
		this.cmdExport.addClickListener(event -> this.cmdExport_buttonClick(event));
		this.cmdInfoLine.addClickListener(event -> this.cmdInfoLine_buttonClick(event));
		this.tableLine.addItemClickListener(event -> this.tableLine_itemClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdUpdate, cmdReload, cmdReport, cmdInfo, cmdNewLine, cmdDeleteLine,
			cmdUpdateLine, cmdExcel, cmdCopyLine, cmdExport, cmdInfoLine;
	private XdevHorizontalLayout actionLayout, horizontalLayout;
	private XdevTabSheet tabSheet;
	private XdevVerticalLayout verticalLayout, verticalLayoutRight, verticalLayoutReports;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevTable<ProjectLine> tableLine;
	private XdevContainerFilterComponent containerFilterComponent, containerFilterComponent2;
	private XdevTable<Periode> table;
	// </generated-code>

}
