package thaumicenergistics.api;

/**
 * @author BrockWS
 */
public interface IThELangKey {

    String getUnlocalizedKey();

    String getLocalizedKey(Object... args);
}
