package ch.xwr.seicentobilling.ui.desktop;

import java.time.LocalDate;
import java.util.Date;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import com.xdev.ui.XdevVerticalLayout;
import com.xdev.ui.XdevView;
import com.xdev.ui.entitycomponent.table.XdevTable;
import com.xdev.ui.filter.FilterData;
import com.xdev.ui.filter.FilterOperator;
import com.xdev.ui.filter.XdevContainerFilterComponent;
import com.xdev.ui.util.NestedProperty;

import ch.xwr.seicentobilling.entities.Entity_;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowObject_;

public class RowObjectTabView extends XdevView {

	/**
	 *
	 */
	public RowObjectTabView() {
		super();
		this.initUI();

		//Sort it
		final Object [] properties={"objChanged","objRowId"};
		final boolean [] ordering={false, false};
		this.table.sort(properties, ordering);

		setDefaultFilter();

	}

	private void setDefaultFilter() {
		final LocalDate ld1 = LocalDate.now().minusDays(1);
		final Date yesterday = java.sql.Timestamp.valueOf(ld1.atStartOfDay());
		final Date[] dateArray = new Date[] { yesterday };

		final FilterData[] fd = new FilterData[]{new FilterData("objChanged", new FilterOperator.Greater(), dateArray)};
		this.containerFilterComponent.setFilterData(fd);
	}


	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #table}.
	 *
	 * @see ItemClickEvent.ItemClickListener#itemClick(ItemClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_itemClick(final ItemClickEvent event) {
		Notification.show("Event Triggered " + event.getButtonName(), Notification.Type.TRAY_NOTIFICATION);

		if (event.isDoubleClick()) {
			Notification.show("Event Triggered ", Notification.Type.TRAY_NOTIFICATION);

//			final RowObjectDAO dao = new RowObjectDAO();
//			final RowObject bean = dao.find((Long)event.getItemId());
			final RowObject bean = (RowObject) event.getItemId();

			final Window win = RowObjectView.getPopupWindow();

			win.setContent(new RowObjectView(bean.getObjRowId(), bean.getEntity().getEntName()));
			this.getUI().addWindow(win);
		}

	}



	/**
	 * Event handler delegate method for the {@link XdevTable}
	 * {@link #table}.
	 *
	 * @see Component.Listener#componentEvent(Component.Event)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void table_componentEvent(final Component.Event event) {
		Notification.show("Component Triggered ", Notification.Type.TRAY_NOTIFICATION);

	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.verticalLayout = new XdevVerticalLayout();
		this.containerFilterComponent = new XdevContainerFilterComponent();
		this.table = new XdevTable<>();

		this.table.setContainerDataSource(RowObject.class, NestedProperty.of(RowObject_.entity, Entity_.entName));
		this.table.setVisibleColumns(NestedProperty.path(RowObject_.entity, Entity_.entName), RowObject_.objRowId.getName(),
				RowObject_.objChngcnt.getName(), RowObject_.objAdded.getName(), RowObject_.objAddedBy.getName(),
				RowObject_.objChanged.getName(), RowObject_.objChangedBy.getName(), RowObject_.objDeleted.getName(),
				RowObject_.objDeletedBy.getName(), RowObject_.objState.getName());
		this.table.setColumnHeader("entity.entName", "Tabelle");
		this.table.setColumnHeader("objRowId", "Objekt Id");
		this.table.setColumnHeader("objChngcnt", "Anderungszähler");
		this.table.setColumnHeader("objAdded", "Erstellt am");
		this.table.setColumnHeader("objAddedBy", "Erstellt von");
		this.table.setColumnHeader("objChanged", "Geändert am");
		this.table.setColumnHeader("objChangedBy", "Geändert von");
		this.table.setColumnHeader("objDeleted", "Gelöscht am");
		this.table.setColumnHeader("objDeletedBy", "Gelöscht von");
		this.table.setColumnHeader("objState", "Status");

		this.containerFilterComponent.setContainer(this.table.getBeanContainerDataSource(), "objRowId", "objChngcnt",
				"objAdded", "objAddedBy", "objChanged", "objChangedBy", "objDeleted", "objDeletedBy", "objState",
				"entity.entName");
		this.containerFilterComponent.setSearchableProperties("objAddedBy", "objChangedBy", "objDeletedBy");

		this.containerFilterComponent.setWidth(100, Unit.PERCENTAGE);
		this.containerFilterComponent.setHeight(-1, Unit.PIXELS);
		this.verticalLayout.addComponent(this.containerFilterComponent);
		this.verticalLayout.setComponentAlignment(this.containerFilterComponent, Alignment.MIDDLE_CENTER);
		this.table.setSizeFull();
		this.verticalLayout.addComponent(this.table);
		this.verticalLayout.setComponentAlignment(this.table, Alignment.MIDDLE_CENTER);
		this.verticalLayout.setExpandRatio(this.table, 100.0F);
		this.verticalLayout.setSizeFull();
		this.setContent(this.verticalLayout);
		this.setSizeFull();

		this.table.addItemClickListener(event -> this.table_itemClick(event));
		this.table.addListener((Component.Listener) event -> this.table_componentEvent(event));
	} // </generated-code>


	// <generated-code name="variables">
	private XdevTable<RowObject> table;
	private XdevVerticalLayout verticalLayout;
	private XdevContainerFilterComponent containerFilterComponent;
	// </generated-code>

}
