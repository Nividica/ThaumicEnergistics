package thaumicenergistics.lang;

import thaumicenergistics.api.IThELang;
import thaumicenergistics.api.IThELangKey;

/**
 * @author BrockWS
 */
public class ThELang implements IThELang {

    private IThELangKey creativeTab;
    private IThELangKey tileInfusionProvider;
    private IThELangKey tileArcaneAssembler;
    private IThELangKey itemDiffusionCore;
    private IThELangKey itemCoalescenceCore;
    private IThELangKey itemEssentia1kComponent;
    private IThELangKey itemEssentia4kComponent;
    private IThELangKey itemEssentia16kComponent;
    private IThELangKey itemEssentia64kComponent;
    private IThELangKey itemEssentia1kCell;
    private IThELangKey itemEssentia4kCell;
    private IThELangKey itemEssentia16kCell;
    private IThELangKey itemEssentia64kCell;
    private IThELangKey itemEssentiaImportBus;
    private IThELangKey itemEssentiaExportBus;
    private IThELangKey itemEssentiaStorageBus;
    private IThELangKey itemEssentiaTerminal;
    private IThELangKey itemArcaneTerminal;
    private IThELangKey itemArcaneChargingUpgrade;
    private IThELangKey itemKnowledgeCore;
    private IThELangKey itemBlankKnowledgeCore;
    private IThELangKey tooltipWIP;
    private IThELangKey deviceOnline;
    private IThELangKey deviceOffline;
    private IThELangKey deviceMissingChannel;
    private IThELangKey arcaneAssemblerIdle;
    private IThELangKey arcaneAssemblerPrep;
    private IThELangKey arcaneAssemblerBusy;
    private IThELangKey arcaneAssemblerProgress;
    private IThELangKey arcaneAssemblerNoAspect;
    private IThELangKey arcaneAssemblerNoVis;
    private IThELangKey guiEssentiaImportBus;
    private IThELangKey guiEssentiaExportBus;
    private IThELangKey guiEssentiaStorageBus;
    private IThELangKey guiEssentiaTerminal;
    private IThELangKey guiArcaneTerminal;
    private IThELangKey guiArcaneInscriber;
    private IThELangKey guiVisRequired;
    private IThELangKey guiVisRequiredOutOf;
    private IThELangKey guiVisAvailable;
    private IThELangKey guiVisDiscount;
    private IThELangKey guiInsertKnowledgeCore;
    private IThELangKey guiKnowledgeCoreBlank;
    private IThELangKey guiRecipeAlreadyStored;
    private IThELangKey guiRecipeNotArcane;
    private IThELangKey guiNoRecipe;
    private IThELangKey guiOutOfAspect;
    private IThELangKey guiOutOfVis;
    private IThELangKey researchCategory;

    public ThELang() {
        this.creativeTab = new ThELangKey("itemGroup.ThaumicEnergistics");

        this.tileInfusionProvider = new ThELangKey("tile.thaumicenergistics.infusion_provider.name");
        this.tileArcaneAssembler = new ThELangKey("tile.thaumicenergistics.arcane_assembler.name");

        this.itemDiffusionCore = new ThELangKey("item.thaumicenergistics.diffusion_core.name");
        this.itemCoalescenceCore = new ThELangKey("item.thaumicenergistics.coalescence_core.name");

        this.itemEssentia1kComponent = new ThELangKey("item.thaumicenergistics.essentia_component_1k.name");
        this.itemEssentia4kComponent = new ThELangKey("item.thaumicenergistics.essentia_component_4k.name");
        this.itemEssentia16kComponent = new ThELangKey("item.thaumicenergistics.essentia_component_16k.name");
        this.itemEssentia64kComponent = new ThELangKey("item.thaumicenergistics.essentia_component_64k.name");

        this.itemEssentia1kCell = new ThELangKey("item.thaumicenergistics.essentia_cell_1k.name");
        this.itemEssentia4kCell = new ThELangKey("item.thaumicenergistics.essentia_cell_4k.name");
        this.itemEssentia16kCell = new ThELangKey("item.thaumicenergistics.essentia_cell_16k.name");
        this.itemEssentia64kCell = new ThELangKey("item.thaumicenergistics.essentia_cell_64k.name");

        this.itemEssentiaImportBus = new ThELangKey("item.thaumicenergistics.essentia_import.name");
        this.itemEssentiaExportBus = new ThELangKey("item.thaumicenergistics.essentia_export.name");
        this.itemEssentiaStorageBus = new ThELangKey("item.thaumicenergistics.essentia_storage.name");
        this.itemEssentiaTerminal = new ThELangKey("item.thaumicenergistics.essentia_terminal.name");
        this.itemArcaneTerminal = new ThELangKey("item.thaumicenergistics.arcane_terminal.name");

        this.itemArcaneChargingUpgrade = new ThELangKey("item.thaumicenergistics.upgrade_arcane.name");
        this.itemKnowledgeCore = new ThELangKey("item.thaumicenergistics.knowledge_core.name");
        this.itemBlankKnowledgeCore = new ThELangKey("item.thaumicenergistics.blank_knowledge_core.name");

        this.tooltipWIP = new ThELangKey("tooltip.thaumicenergistics.wip");
        this.deviceOnline = new ThELangKey("tooltip.thaumicenergistics.device_online");
        this.deviceOffline = new ThELangKey("tooltip.thaumicenergistics.device_offline");
        this.deviceMissingChannel = new ThELangKey("tooltip.thaumicenergistics.device_missing_channel");
        this.arcaneAssemblerIdle = new ThELangKey("tooltip.thaumicenergistics.arcane_assembler.idle");
        this.arcaneAssemblerPrep = new ThELangKey("tooltip.thaumicenergistics.arcane_assembler.prep");
        this.arcaneAssemblerBusy = new ThELangKey("tooltip.thaumicenergistics.arcane_assembler.busy");
        this.arcaneAssemblerProgress = new ThELangKey("tooltip.thaumicenergistics.arcane_assembler.progress");
        this.arcaneAssemblerNoAspect = new ThELangKey("tooltip.thaumicenergistics.arcane_assembler.no_aspect");
        this.arcaneAssemblerNoVis = new ThELangKey("tooltip.thaumicenergistics.arcane_assembler.no_vis");

        this.guiEssentiaImportBus = new ThELangKey("gui.thaumicenergistics.essentia_import_bus");
        this.guiEssentiaExportBus = new ThELangKey("gui.thaumicenergistics.essentia_export_bus");
        this.guiEssentiaStorageBus = new ThELangKey("gui.thaumicenergistics.essentia_storage_bus");
        this.guiEssentiaTerminal = new ThELangKey("gui.thaumicenergistics.essentia_terminal");
        this.guiArcaneTerminal = new ThELangKey("gui.thaumicenergistics.arcane_terminal");
        this.guiArcaneInscriber = new ThELangKey("gui.thaumicenergistics.arcane_inscriber");

        this.guiVisRequired = new ThELangKey("gui.thaumicenergistics.vis_required");
        this.guiVisRequiredOutOf = new ThELangKey("gui.thaumicenergistics.vis_required_out_of");
        this.guiVisAvailable = new ThELangKey("gui.thaumicenergistics.vis_available");
        this.guiVisDiscount = new ThELangKey("gui.thaumicenergistics.vis_discount");

        this.guiInsertKnowledgeCore = new ThELangKey("gui.thaumicenergistics.insert_knowledge_core");
        this.guiKnowledgeCoreBlank = new ThELangKey("gui.thaumicenergistics.knowledge_core_is_blank");
        this.guiRecipeAlreadyStored = new ThELangKey("gui.thaumicenergistics.recipe_already_stored");
        this.guiRecipeNotArcane = new ThELangKey("gui.thaumicenergistics.recipe_not_arcane");
        this.guiNoRecipe = new ThELangKey("gui.thaumicenergistics.no_recipe");

        this.guiOutOfAspect = new ThELangKey("gui.thaumicenergistics.out_of_aspect");
        this.guiOutOfVis = new ThELangKey("gui.thaumicenergistics.out_of_vis");

        this.researchCategory = new ThELangKey("tc.research_category.THAUMICENERGISTICS");
    }

    @Override
    public IThELangKey creativeTab() {
        return this.creativeTab;
    }

    @Override
    public IThELangKey tileInfusionProvider() {
        return this.tileInfusionProvider;
    }

    @Override
    public IThELangKey tileArcaneAssembler() {
        return this.tileArcaneAssembler;
    }

    @Override
    public IThELangKey itemDiffusionCore() {
        return this.itemDiffusionCore;
    }

    @Override
    public IThELangKey itemCoalescenceCore() {
        return this.itemCoalescenceCore;
    }

    @Override
    public IThELangKey itemEssentia1kComponent() {
        return this.itemEssentia1kComponent;
    }

    @Override
    public IThELangKey itemEssentia4kComponent() {
        return this.itemEssentia4kComponent;
    }

    @Override
    public IThELangKey itemEssentia16kComponent() {
        return this.itemEssentia16kComponent;
    }

    @Override
    public IThELangKey itemEssentia64kComponent() {
        return this.itemEssentia64kComponent;
    }

    @Override
    public IThELangKey itemEssentia1kCell() {
        return this.itemEssentia1kCell;
    }

    @Override
    public IThELangKey itemEssentia4kCell() {
        return this.itemEssentia4kCell;
    }

    @Override
    public IThELangKey itemEssentia16kCell() {
        return this.itemEssentia16kCell;
    }

    @Override
    public IThELangKey itemEssentia64kCell() {
        return this.itemEssentia64kCell;
    }

    @Override
    public IThELangKey itemEssentiaImportBus() {
        return this.itemEssentiaImportBus;
    }

    @Override
    public IThELangKey itemEssentiaExportBus() {
        return this.itemEssentiaExportBus;
    }

    @Override
    public IThELangKey itemEssentiaStorageBus() {
        return this.itemEssentiaStorageBus;
    }

    @Override
    public IThELangKey itemEssentiaTerminal() {
        return this.itemEssentiaTerminal;
    }

    @Override
    public IThELangKey itemArcaneTerminal() {
        return this.itemArcaneTerminal;
    }

    @Override
    public IThELangKey itemArcaneChargingUpgrade() {
        return this.itemArcaneChargingUpgrade;
    }

    @Override
    public IThELangKey itemKnowledgeCore() {
        return this.itemKnowledgeCore;
    }

    @Override
    public IThELangKey itemBlankKnowledgeCore() {
        return this.itemBlankKnowledgeCore;
    }

    @Override
    public IThELangKey tooltipWIP() {
        return this.tooltipWIP;
    }

    @Override
    public IThELangKey deviceOnline() {
        return this.deviceOnline;
    }

    @Override
    public IThELangKey deviceOffline() {
        return this.deviceOffline;
    }

    @Override
    public IThELangKey deviceMissingChannel() {
        return this.deviceMissingChannel;
    }

    @Override
    public IThELangKey arcaneAssemblerIdle() {
        return this.arcaneAssemblerIdle;
    }

    @Override
    public IThELangKey arcaneAssemblerPrep() {
        return this.arcaneAssemblerPrep;
    }

    @Override
    public IThELangKey arcaneAssemblerBusy() {
        return this.arcaneAssemblerBusy;
    }

    @Override
    public IThELangKey arcaneAssemblerProgress() {
        return this.arcaneAssemblerProgress;
    }

    @Override
    public IThELangKey arcaneAssemblerNoAspect() {
        return this.arcaneAssemblerNoAspect;
    }

    @Override
    public IThELangKey arcaneAssemblerNoVis() {
        return this.arcaneAssemblerNoVis;
    }

    @Override
    public IThELangKey guiEssentiaImportBus() {
        return this.guiEssentiaImportBus;
    }

    @Override
    public IThELangKey guiEssentiaExportBus() {
        return this.guiEssentiaExportBus;
    }

    @Override
    public IThELangKey guiEssentiaStorageBus() {
        return this.guiEssentiaStorageBus;
    }

    @Override
    public IThELangKey guiEssentiaTerminal() {
        return this.guiEssentiaTerminal;
    }

    @Override
    public IThELangKey guiArcaneTerminal() {
        return this.guiArcaneTerminal;
    }

    @Override
    public IThELangKey guiArcaneInscriber() {
        return this.guiArcaneInscriber;
    }

    @Override
    public IThELangKey guiVisRequired() {
        return this.guiVisRequired;
    }

    @Override
    public IThELangKey guiVisRequiredOutOf() {
        return this.guiVisRequiredOutOf;
    }

    @Override
    public IThELangKey guiVisAvailable() {
        return this.guiVisAvailable;
    }

    @Override
    public IThELangKey guiVisDiscount() {
        return this.guiVisDiscount;
    }

    @Override
    public IThELangKey guiInsertKnowledgeCore() {
        return this.guiInsertKnowledgeCore;
    }

    @Override
    public IThELangKey guiKnowledgeCoreBlank() {
        return this.guiKnowledgeCoreBlank;
    }

    @Override
    public IThELangKey guiRecipeAlreadyStored() {
        return this.guiRecipeAlreadyStored;
    }

    @Override
    public IThELangKey guiRecipeNotArcane() {
        return this.guiRecipeNotArcane;
    }

    @Override
    public IThELangKey guiNoRecipe() {
        return this.guiNoRecipe;
    }

    @Override
    public IThELangKey guiOutOfAspect() {
        return this.guiOutOfAspect;
    }

    @Override
    public IThELangKey guiOutOfVis() {
        return this.guiOutOfVis;
    }

    @Override
    public IThELangKey researchCategory() {
        return this.researchCategory;
    }
}
