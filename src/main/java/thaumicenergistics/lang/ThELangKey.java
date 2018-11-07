package thaumicenergistics.lang;


import net.minecraft.client.resources.I18n;

import thaumicenergistics.api.IThELangKey;

/**
 * @author BrockWS
 */
public class ThELangKey implements IThELangKey {

    private String key;

    protected ThELangKey(String key) {
        this.key = key;
    }

    @Override
    public String getUnlocalizedKey() {
        return this.key;
    }

    @Override
    public String getLocalizedKey() {
        if (I18n.hasKey(this.getUnlocalizedKey()))
            return I18n.format(this.getUnlocalizedKey());
        return this.getUnlocalizedKey();
    }
}
