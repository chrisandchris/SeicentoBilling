package ch.xwr.seicentobilling.ui.desktop;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.xdev.dal.DAOs;
import com.xdev.res.ApplicationResource;
import com.xdev.res.StringResourceUtils;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevFieldGroup;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevHorizontalSplitPanel;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPasswordField;
import com.xdev.ui.XdevTabSheet;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.masterdetail.MasterDetail;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.dal.CompanyDAO;
import ch.xwr.seicentobilling.entities.Company;
import ch.xwr.seicentobilling.entities.Company_;

public class CompanyTabView extends XdevView {

	/**
	 *
	 */
	public CompanyTabView() {
		super();
		this.initUI();
	}

	/**
	 * Event handler delegate method for the {@link XdevTable} {@link #table}.
	 *
	 * @see Property.ValueChangeListener#valueChange(Property.ValueChangeEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_valueChange(final Property.ValueChangeEvent event) {
		//setROFields();

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdSave}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.save();

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(this.fieldGroup.getItemDataSource().getBean().getCmpId(),
				this.fieldGroup.getItemDataSource().getBean().getClass().getSimpleName());

		Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReset}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_buttonClick(final Button.ClickEvent event) {
		this.fieldGroup.discard();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdNew}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNew_buttonClick(final Button.ClickEvent event) {
		final Company cmp = new Company();
		cmp.setCmpState(LovState.State.inactive);
		cmp.setCmpBookingYear(2019);
		cmp.setCmpLastCustomerNbr(1000);
		cmp.setCmpLastItemNbr(1000);
		cmp.setCmpLastOrderNbr(100);

		this.fieldGroup.setItemDataSource(cmp);
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdDelete}.
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
				final Company bean = CompanyTabView.this.table.getSelectedItem().getBean();
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getCmpId(), bean.getClass().getSimpleName());

				final CompanyDAO dao = new CompanyDAO();
				dao.remove(bean);
				CompanyTabView.this.table.getBeanContainerDataSource().refresh();

				try {
					CompanyTabView.this.table.select(CompanyTabView.this.table.getCurrentPageFirstItemId());
				} catch (final Exception e) {
					//ignore
					CompanyTabView.this.fieldGroup.setItemDataSource(new Company());
				}
				Notification.show("Datensatz löschen", "Datensatz wurde gelöscht!", Notification.Type.TRAY_NOTIFICATION);
			}

		});
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdReload}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReload_buttonClick(final Button.ClickEvent event) {
		this.table.refreshRowCache();
		this.table.getBeanContainerDataSource().refresh();
		this.table.sort();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdInfo}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdInfo_buttonClick(final Button.ClickEvent event) {
		final Company bean = this.fieldGroup.getItemDataSource().getBean();

		final Window win = RowObjectView.getPopupWindow();

		//UI.getCurrent().getSession().setAttribute(String.class, bean.getClass().getSimpleName());
		win.setContent(new RowObjectView(bean.getCmpId(), bean.getClass().getSimpleName()));
		this.getUI().addWindow(win);
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
		this.cmdReload = new XdevButton();
		this.cmdInfo = new XdevButton();
		this.table = new XdevTable<>();
		this.gridLayoutData = new XdevGridLayout();
		this.tabSheet = new XdevTabSheet();
		this.gridLayout = new XdevGridLayout();
		this.lblCmpName = new XdevLabel();
		this.txtCmpName = new XdevTextField();
		this.lblCmpAddress = new XdevLabel();
		this.txtCmpAddress = new XdevTextField();
		this.lblCmpZip = new XdevLabel();
		this.txtCmpZip = new XdevTextField();
		this.lblCmpPlace = new XdevLabel();
		this.txtCmpPlace = new XdevTextField();
		this.lblCmpVatcode = new XdevLabel();
		this.txtCmpVatcode = new XdevTextField();
		this.lblCmpCurrency = new XdevLabel();
		this.txtCmpCurrency = new XdevTextField();
		this.lblCmpUid = new XdevLabel();
		this.txtCmpUid = new XdevTextField();
		this.lblCmpPhone = new XdevLabel();
		this.txtCmpPhone = new XdevTextField();
		this.lblCmpMail = new XdevLabel();
		this.txtCmpMail = new XdevTextField();
		this.lblCmpComm1 = new XdevLabel();
		this.txtCmpComm1 = new XdevTextField();
		this.lblCmpBusiness = new XdevLabel();
		this.txtCmpBusiness = new XdevTextField();
		this.gridLayoutNbr = new XdevGridLayout();
		this.lblCmpBookingYear = new XdevLabel();
		this.txtCmpBookingYear = new XdevTextField();
		this.lblCmpLastOrderNbr = new XdevLabel();
		this.txtCmpLastOrderNbr = new XdevTextField();
		this.lblCmpLastItemNbr = new XdevLabel();
		this.txtCmpLastItemNbr = new XdevTextField();
		this.lblCmpLastCustomerNbr = new XdevLabel();
		this.txtCmpLastCustomerNbr = new XdevTextField();
		this.gridLayoutJasper = new XdevGridLayout();
		this.lblCmpJasperUri2 = new XdevLabel();
		this.txtCmpJasperUri = new XdevTextField();
		this.lblCmpReportUsr2 = new XdevLabel();
		this.txtCmpReportUsr = new XdevTextField();
		this.lblCmpReportPwd2 = new XdevLabel();
		this.passwordField = new XdevPasswordField();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdSave = new XdevButton();
		this.cmdReset = new XdevButton();
		this.fieldGroup = new XdevFieldGroup<>(Company.class);

		this.horizontalSplitPanel.setStyleName("large");
		this.horizontalSplitPanel.setSplitPosition(50.0F, Unit.PERCENTAGE);
		this.verticalLayout.setMargin(new MarginInfo(false));
		this.actionLayout.setSpacing(false);
		this.actionLayout.setMargin(new MarginInfo(false));
		this.cmdNew.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/new1_16.png"));
		this.cmdNew.setDescription("Neuen Datensatz anlegen");
		this.cmdDelete
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/delete3_16.png"));
		this.cmdReload.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/reload2.png"));
		this.cmdInfo
				.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/info_small.jpg"));
		this.table.setColumnReorderingAllowed(true);
		this.table.setColumnCollapsingAllowed(true);
		this.table.setContainerDataSource(Company.class, DAOs.get(CompanyDAO.class).findAll());
		this.table.addGeneratedColumn("generated", new FunctionActivateCompany.Generator());
		this.table.setVisibleColumns(Company_.cmpName.getName(), Company_.cmpUid.getName(),
				Company_.cmpBookingYear.getName(), Company_.cmpMail.getName(), Company_.cmpPhone.getName(), "generated");
		this.table.setColumnHeader("cmpName", "Name");
		this.table.setColumnHeader("cmpUid", "Uid");
		this.table.setColumnHeader("cmpBookingYear", "BH Jahr");
		this.table.setConverter("cmpBookingYear", ConverterBuilder.stringToInteger().groupingUsed(false).build());
		this.table.setColumnCollapsed("cmpBookingYear", true);
		this.table.setColumnHeader("cmpMail", "Mail");
		this.table.setColumnCollapsed("cmpMail", true);
		this.table.setColumnHeader("cmpPhone", "Telefon");
		this.table.setColumnCollapsed("cmpPhone", true);
		this.table.setColumnHeader("generated", " ");
		this.gridLayoutData.setMargin(new MarginInfo(false));
		this.tabSheet.setStyleName("framed");
		this.lblCmpName.setValue(StringResourceUtils.optLocalizeString("{$lblCmpName.value}", this));
		this.lblCmpAddress.setValue(StringResourceUtils.optLocalizeString("{$lblCmpAddress.value}", this));
		this.lblCmpZip.setValue(StringResourceUtils.optLocalizeString("{$lblCmpZip.value}", this));
		this.lblCmpPlace.setValue(StringResourceUtils.optLocalizeString("{$lblCmpPlace.value}", this));
		this.lblCmpVatcode.setValue(StringResourceUtils.optLocalizeString("{$lblCmpVatcode.value}", this));
		this.lblCmpCurrency.setValue(StringResourceUtils.optLocalizeString("{$lblCmpCurrency.value}", this));
		this.lblCmpUid.setValue(StringResourceUtils.optLocalizeString("{$lblCmpUid.value}", this));
		this.lblCmpPhone.setValue(StringResourceUtils.optLocalizeString("{$lblCmpPhone.value}", this));
		this.lblCmpMail.setValue(StringResourceUtils.optLocalizeString("{$lblCmpMail.value}", this));
		this.lblCmpComm1.setValue(StringResourceUtils.optLocalizeString("{$lblCmpComm1.value}", this));
		this.lblCmpBusiness.setValue(StringResourceUtils.optLocalizeString("{$lblCmpBusiness.value}", this));
		this.lblCmpBookingYear.setValue(StringResourceUtils.optLocalizeString("{$lblCmpBookingYear.value}", this));
		this.txtCmpBookingYear.setConverter(ConverterBuilder.stringToInteger().groupingUsed(false).build());
		this.lblCmpLastOrderNbr.setValue(StringResourceUtils.optLocalizeString("{$lblCmpLastOrderNbr.value}", this));
		this.txtCmpLastOrderNbr.setConverter(ConverterBuilder.stringToBigInteger().build());
		this.lblCmpLastItemNbr.setValue(StringResourceUtils.optLocalizeString("{$lblCmpLastItemNbr.value}", this));
		this.txtCmpLastItemNbr.setConverter(ConverterBuilder.stringToBigInteger().build());
		this.lblCmpLastCustomerNbr.setValue(StringResourceUtils.optLocalizeString("{$lblCmpLastCustomerNbr.value}", this));
		this.txtCmpLastCustomerNbr.setConverter(ConverterBuilder.stringToBigInteger().build());
		this.lblCmpJasperUri2.setValue(StringResourceUtils.optLocalizeString("{$lblCmpJasperUri.value}", this));
		this.lblCmpReportUsr2.setValue(StringResourceUtils.optLocalizeString("{$lblCmpReportUsr.value}", this));
		this.lblCmpReportPwd2.setValue(StringResourceUtils.optLocalizeString("{$lblCmpReportPwd.value}", this));
		this.horizontalLayout.setMargin(new MarginInfo(false, false, true, true));
		this.cmdSave.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/save1.png"));
		this.cmdSave.setCaption(StringResourceUtils.optLocalizeString("{$cmdSave.caption}", this));
		this.cmdReset.setIcon(new ApplicationResource(this.getClass(), "WebContent/WEB-INF/resources/images/cancel1.png"));
		this.cmdReset.setCaption(StringResourceUtils.optLocalizeString("{$cmdReset.caption}", this));
		this.fieldGroup.bind(this.txtCmpName, Company_.cmpName.getName());
		this.fieldGroup.bind(this.txtCmpAddress, Company_.cmpAddress.getName());
		this.fieldGroup.bind(this.txtCmpZip, Company_.cmpZip.getName());
		this.fieldGroup.bind(this.txtCmpPlace, Company_.cmpPlace.getName());
		this.fieldGroup.bind(this.txtCmpVatcode, Company_.cmpVatcode.getName());
		this.fieldGroup.bind(this.txtCmpCurrency, Company_.cmpCurrency.getName());
		this.fieldGroup.bind(this.txtCmpUid, Company_.cmpUid.getName());
		this.fieldGroup.bind(this.txtCmpPhone, Company_.cmpPhone.getName());
		this.fieldGroup.bind(this.txtCmpMail, Company_.cmpMail.getName());
		this.fieldGroup.bind(this.txtCmpComm1, Company_.cmpComm1.getName());
		this.fieldGroup.bind(this.txtCmpBusiness, Company_.cmpBusiness.getName());
		this.fieldGroup.bind(this.txtCmpBookingYear, Company_.cmpBookingYear.getName());
		this.fieldGroup.bind(this.txtCmpLastOrderNbr, Company_.cmpLastOrderNbr.getName());
		this.fieldGroup.bind(this.txtCmpLastItemNbr, Company_.cmpLastItemNbr.getName());
		this.fieldGroup.bind(this.txtCmpLastCustomerNbr, Company_.cmpLastCustomerNbr.getName());
		this.fieldGroup.bind(this.txtCmpJasperUri, Company_.cmpJasperUri.getName());
		this.fieldGroup.bind(this.txtCmpReportUsr, Company_.cmpReportUsr.getName());
		this.fieldGroup.bind(this.passwordField, Company_.cmpReportPwd.getName());

		MasterDetail.connect(this.table, this.fieldGroup);

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "cmpPlace", "cmpZip",
				"cmpState");
		this.containerFilterComponent.setSearchableProperties("cmpName", "cmpPlace");

		this.cmdNew.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdNew);
		this.actionLayout.setComponentAlignment(this.cmdNew, Alignment.MIDDLE_CENTER);
		this.cmdDelete.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdDelete);
		this.actionLayout.setComponentAlignment(this.cmdDelete, Alignment.MIDDLE_CENTER);
		this.cmdReload.setSizeUndefined();
		this.actionLayout.addComponent(this.cmdReload);
		this.actionLayout.setComponentAlignment(this.cmdReload, Alignment.MIDDLE_CENTER);
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
		this.gridLayout.setColumns(2);
		this.gridLayout.setRows(12);
		this.lblCmpName.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpName, 0, 0);
		this.txtCmpName.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpName.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpName, 1, 0);
		this.lblCmpAddress.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpAddress, 0, 1);
		this.txtCmpAddress.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpAddress.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpAddress, 1, 1);
		this.lblCmpZip.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpZip, 0, 2);
		this.txtCmpZip.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpZip.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpZip, 1, 2);
		this.lblCmpPlace.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpPlace, 0, 3);
		this.txtCmpPlace.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpPlace.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpPlace, 1, 3);
		this.lblCmpVatcode.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpVatcode, 0, 4);
		this.txtCmpVatcode.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpVatcode.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpVatcode, 1, 4);
		this.lblCmpCurrency.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpCurrency, 0, 5);
		this.txtCmpCurrency.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpCurrency.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpCurrency, 1, 5);
		this.lblCmpUid.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpUid, 0, 6);
		this.txtCmpUid.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpUid.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpUid, 1, 6);
		this.lblCmpPhone.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpPhone, 0, 7);
		this.txtCmpPhone.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpPhone.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpPhone, 1, 7);
		this.lblCmpMail.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpMail, 0, 8);
		this.txtCmpMail.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpMail.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpMail, 1, 8);
		this.lblCmpComm1.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpComm1, 0, 9);
		this.txtCmpComm1.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpComm1.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpComm1, 1, 9);
		this.lblCmpBusiness.setSizeUndefined();
		this.gridLayout.addComponent(this.lblCmpBusiness, 0, 10);
		this.txtCmpBusiness.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpBusiness.setHeight(-1, Unit.PIXELS);
		this.gridLayout.addComponent(this.txtCmpBusiness, 1, 10);
		this.gridLayout.setColumnExpandRatio(1, 100.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 11, 1, 11);
		this.gridLayout.setRowExpandRatio(11, 1.0F);
		this.gridLayoutNbr.setColumns(2);
		this.gridLayoutNbr.setRows(5);
		this.lblCmpBookingYear.setSizeUndefined();
		this.gridLayoutNbr.addComponent(this.lblCmpBookingYear, 0, 0);
		this.txtCmpBookingYear.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpBookingYear.setHeight(-1, Unit.PIXELS);
		this.gridLayoutNbr.addComponent(this.txtCmpBookingYear, 1, 0);
		this.lblCmpLastOrderNbr.setSizeUndefined();
		this.gridLayoutNbr.addComponent(this.lblCmpLastOrderNbr, 0, 1);
		this.txtCmpLastOrderNbr.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpLastOrderNbr.setHeight(-1, Unit.PIXELS);
		this.gridLayoutNbr.addComponent(this.txtCmpLastOrderNbr, 1, 1);
		this.lblCmpLastItemNbr.setSizeUndefined();
		this.gridLayoutNbr.addComponent(this.lblCmpLastItemNbr, 0, 2);
		this.txtCmpLastItemNbr.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpLastItemNbr.setHeight(-1, Unit.PIXELS);
		this.gridLayoutNbr.addComponent(this.txtCmpLastItemNbr, 1, 2);
		this.lblCmpLastCustomerNbr.setSizeUndefined();
		this.gridLayoutNbr.addComponent(this.lblCmpLastCustomerNbr, 0, 3);
		this.txtCmpLastCustomerNbr.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpLastCustomerNbr.setHeight(-1, Unit.PIXELS);
		this.gridLayoutNbr.addComponent(this.txtCmpLastCustomerNbr, 1, 3);
		this.gridLayoutNbr.setColumnExpandRatio(1, 100.0F);
		final CustomComponent gridLayoutNbr_vSpacer = new CustomComponent();
		gridLayoutNbr_vSpacer.setSizeFull();
		this.gridLayoutNbr.addComponent(gridLayoutNbr_vSpacer, 0, 4, 1, 4);
		this.gridLayoutNbr.setRowExpandRatio(4, 1.0F);
		this.gridLayoutJasper.setColumns(2);
		this.gridLayoutJasper.setRows(4);
		this.lblCmpJasperUri2.setSizeUndefined();
		this.gridLayoutJasper.addComponent(this.lblCmpJasperUri2, 0, 0);
		this.txtCmpJasperUri.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpJasperUri.setHeight(-1, Unit.PIXELS);
		this.gridLayoutJasper.addComponent(this.txtCmpJasperUri, 1, 0);
		this.lblCmpReportUsr2.setSizeUndefined();
		this.gridLayoutJasper.addComponent(this.lblCmpReportUsr2, 0, 1);
		this.txtCmpReportUsr.setWidth(100, Unit.PERCENTAGE);
		this.txtCmpReportUsr.setHeight(-1, Unit.PIXELS);
		this.gridLayoutJasper.addComponent(this.txtCmpReportUsr, 1, 1);
		this.lblCmpReportPwd2.setSizeUndefined();
		this.gridLayoutJasper.addComponent(this.lblCmpReportPwd2, 0, 2);
		this.passwordField.setSizeUndefined();
		this.gridLayoutJasper.addComponent(this.passwordField, 1, 2);
		this.gridLayoutJasper.setColumnExpandRatio(1, 100.0F);
		final CustomComponent gridLayoutJasper_vSpacer = new CustomComponent();
		gridLayoutJasper_vSpacer.setSizeFull();
		this.gridLayoutJasper.addComponent(gridLayoutJasper_vSpacer, 0, 3, 1, 3);
		this.gridLayoutJasper.setRowExpandRatio(3, 1.0F);
		this.gridLayout.setSizeFull();
		this.tabSheet.addTab(this.gridLayout, StringResourceUtils.optLocalizeString("{$gridLayout.caption}", this), null);
		this.gridLayoutNbr.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutNbr, StringResourceUtils.optLocalizeString("{$gridLayoutNbr.caption}", this),
				null);
		this.gridLayoutJasper.setSizeFull();
		this.tabSheet.addTab(this.gridLayoutJasper, "Jasper", null);
		this.tabSheet.setSelectedTab(this.gridLayout);
		this.cmdSave.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdSave);
		this.horizontalLayout.setComponentAlignment(this.cmdSave, Alignment.MIDDLE_RIGHT);
		this.cmdReset.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdReset);
		this.horizontalLayout.setComponentAlignment(this.cmdReset, Alignment.MIDDLE_LEFT);
		this.gridLayoutData.setColumns(1);
		this.gridLayoutData.setRows(2);
		this.tabSheet.setSizeFull();
		this.gridLayoutData.addComponent(this.tabSheet, 0, 0);
		this.horizontalLayout.setSizeUndefined();
		this.gridLayoutData.addComponent(this.horizontalLayout, 0, 1);
		this.gridLayoutData.setComponentAlignment(this.horizontalLayout, Alignment.TOP_CENTER);
		this.gridLayoutData.setColumnExpandRatio(0, 100.0F);
		this.gridLayoutData.setRowExpandRatio(0, 100.0F);
		this.verticalLayout.setSizeFull();
		this.horizontalSplitPanel.setFirstComponent(this.verticalLayout);
		this.gridLayoutData.setSizeFull();
		this.horizontalSplitPanel.setSecondComponent(this.gridLayoutData);
		this.horizontalSplitPanel.setSizeFull();
		this.setContent(this.horizontalSplitPanel);
		this.setSizeFull();

		this.cmdNew.addClickListener(event -> this.cmdNew_buttonClick(event));
		this.cmdDelete.addClickListener(event -> this.cmdDelete_buttonClick(event));
		this.cmdReload.addClickListener(event -> this.cmdReload_buttonClick(event));
		this.cmdInfo.addClickListener(event -> this.cmdInfo_buttonClick(event));
		this.table.addValueChangeListener(event -> this.table_valueChange(event));
		this.cmdSave.addClickListener(event -> this.cmdSave_buttonClick(event));
		this.cmdReset.addClickListener(event -> this.cmdReset_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevButton cmdNew, cmdDelete, cmdReload, cmdInfo, cmdSave, cmdReset;
	private XdevLabel lblCmpName, lblCmpAddress, lblCmpZip, lblCmpPlace, lblCmpVatcode, lblCmpCurrency, lblCmpUid,
			lblCmpPhone, lblCmpMail, lblCmpComm1, lblCmpBusiness, lblCmpBookingYear, lblCmpLastOrderNbr, lblCmpLastItemNbr,
			lblCmpLastCustomerNbr, lblCmpJasperUri2, lblCmpReportUsr2, lblCmpReportPwd2;
	private XdevTable<Company> table;
	private XdevHorizontalLayout actionLayout, horizontalLayout;
	private XdevPasswordField passwordField;
	private XdevTabSheet tabSheet;
	private XdevFieldGroup<Company> fieldGroup;
	private XdevGridLayout gridLayoutData, gridLayout, gridLayoutNbr, gridLayoutJasper;
	private XdevTextField txtCmpName, txtCmpAddress, txtCmpZip, txtCmpPlace, txtCmpVatcode, txtCmpCurrency, txtCmpUid,
			txtCmpPhone, txtCmpMail, txtCmpComm1, txtCmpBusiness, txtCmpBookingYear, txtCmpLastOrderNbr, txtCmpLastItemNbr,
			txtCmpLastCustomerNbr, txtCmpJasperUri, txtCmpReportUsr;
	private XdevVerticalLayout verticalLayout;
	private XdevHorizontalSplitPanel horizontalSplitPanel;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
