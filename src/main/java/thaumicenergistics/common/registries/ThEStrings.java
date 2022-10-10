package thaumicenergistics.common.registries;

import net.minecraft.util.StatCollector;
import thaumicenergistics.common.ThaumicEnergistics;

/**
 * Houses all used localization strings.
 *
 * @author Nividica
 *
 */
public enum ThEStrings
{
		// Fluids
		Fluid_GaseousEssentia ("fluid.gaseous", false),

		// Blocks
		Block_EssentiaProvider ("block.essentia.provider", true),
		Block_InfusionProvider ("block.infusion.provider", true),
		Block_IronGearbox ("block.gear.box", true),
		Block_ThaumiumGearbox ("block.golem.gear.box", true),
		Block_EssentiaCellWorkbench ("block.essentia.cell.workbench", true),
		Block_ArcaneAssembler ("block.arcane.assembler", true),
		Block_KnowledgeInscriber ("block.knowledge.inscriber", true),
		Block_EssentiaVibrationChamber ("block.essentia.vibration.chamber", true),
		Block_DistillationEncoder ("block.distillation.encoder", true),

		// Parts
		Part_EssentiaImportBus ("aeparts.essentia.ImportBus", true),
		Part_EssentiaExportBus ("aeparts.essentia.ExportBus", true),
		Part_EssentiaLevelEmitter ("aeparts.essentia.levelemitter", true),
		Part_EssentiaStorageBus ("aeparts.essentia.StorageBus", true),
		Part_EssentiaTerminal ("aeparts.essentia.terminal", true),
		Part_ArcaneCraftingTerminal ("aeparts.arcane.crafting.terminal", true),
		Part_VisRelayInterface ("aeparts.vis.interface", true),
		Part_EssentiaStorageMonitor ("aeparts.essentia.storage.monitor", true),
		Part_EssentiaConversionMonitor ("aeparts.essentia.conversion.monitor", true),

		// Items
		Item_EssentiaCell_1k ("item.essentia.cell.1k", true),
		Item_EssentiaCell_4k ("item.essentia.cell.4k", true),
		Item_EssentiaCell_16k ("item.essentia.cell.16k", true),
		Item_EssentiaCell_64k ("item.essentia.cell.64k", true),
		Item_EssentiaCell_Creative ("item.essentia.cell.creative", true),
		Item_EssentiaCellHousing ("item.storage.casing", true),
		Item_StorageComponent_1k ("item.storage.component.1k", true),
		Item_StorageComponent_4k ("item.storage.component.4k", true),
		Item_StorageComponent_16k ("item.storage.component.16k", true),
		Item_StorageComponent_64k ("item.storage.component.64k", true),
		Item_DiffusionCore ("item.material.diffusion.core", true),
		Item_CoalescenceCore ("item.material.coalescence.core", true),
		Item_IronGear ("item.material.iron.gear", true),
		Item_WirelessEssentiaTerminal ("item.wireless.essentia.terminal", true),
		Item_KnowledgeCore ("item.knowledge.core", true),
		Item_FocusAEWrench ("item.focus.aewrench", true),
		Item_FocusAEWrench_Disabled ("item.focus.aewrench.disabled", true),
		Item_Golem_Wifi_Backpack ("item.golem.wifi.backpack", true),

		// Tooltips
		Tooltip_ItemStackDetails ("tooltip.itemstack.details", false),
		Tooltip_CellBytes ("tooltip.essentia.cell.bytes", false),
		Tooltip_CellTypes ("tooltip.essentia.cell.types", false),
		Tooltip_CellContains ("tooltip.essentia.cell.contains", false),
		Tooltip_ArcaneAssemblerHasVis ("tooltip.arcane.assembler.hasVis", false),

		// Button Tooltips
		TooltipButton_VoidHeader ("tooltip.button.void", false),
		TooltipButton_VoidAllow ("tooltip.button.void.allowed", false),
		TooltipButton_VoidDisable ("tooltip.button.void.disabled", false),
		TooltipButton_VoidNote ("tooltip.button.void.note", false),
		TooltipButton_InscriberInvalid ("tooltip.button.inscriber.invalid", false),
		TooltipButton_InscriberFull ("tooltip.button.inscriber.full", false),
		TooltipButton_InscriberDelete ("tooltip.button.inscriber.delete", false),
		TooltipButton_InscriberSave ("tooltip.button.inscriber.save", false),
		TooltipButton_InscriberMissing ("tooltip.button.inscriber.missing", false),
		TooltipButton_SwapArmor ("tooltip.button.swap.armor", false),
		TooltipButton_SwapArmor_Title ("tooltip.button.swap.armor.title", false),

		// GUI
		Gui_TitleArcaneCraftingTerminal ("gui.arcane.crafting.terminal.title", false),
		Gui_TitleEssentiaCell ("gui.essentia.cell.title", false),
		Gui_SelectedAmount ("gui.selected.amount", false),
		Gui_SelectedAspect ("gui.selected.aspect", false),
		Gui_DigiVisSource ("gui.digivis.source", true),
		Gui_TitleEssentiaCellWorkbench ("gui.essentia.cell.workbench.title", false),
		Gui_TitleArcaneAssembler ("gui.arcane.assembler.title", false),
		GUi_VibrationChamber_Stored ("gui.vibration.chamber.stored", false),
		GUi_VibrationChamber_Processing ("gui.vibration.chamber.processing", false);

	private String unlocalized;
	private boolean isDotName;

	private ThEStrings( final String unloc, final boolean isDotName )
	{
		this.unlocalized = ThaumicEnergistics.MOD_ID + "." + unloc;
		this.isDotName = isDotName;
	}

	/**
	 * Gets the localized string.
	 *
	 * @return
	 */
	public String getLocalized()
	{
		if( this.isDotName )
		{
			return StatCollector.translateToLocal( this.unlocalized + ".name" );
		}

		return StatCollector.translateToLocal( this.unlocalized );
	}

	/**
	 * Gets the unlocalized string.
	 *
	 * @return
	 */
	public String getUnlocalized()
	{
		return this.unlocalized;
	}
}
