package thaumicenergistics.api;

import java.lang.reflect.Method;

/**
 * API entry point
 *
 * @author BrockWS
 * @version 1.0.0
 * @since 1.0.0
 */
public class ThEApi {

    private static IThEApi API;

    /**
     * Gets the instance of the Thaumic Energistics API, will cache it if it isn't cached
     *
     * @return API Instance
     */
    public static IThEApi instance() {
        if (ThEApi.API == null) {
            try {
                Class clazz = Class.forName("thaumicenergistics.ThaumicEnergisticsApi");
                Method instanceAccessor = clazz.getMethod("instance");
                ThEApi.API = (IThEApi) instanceAccessor.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ThEApi.API;
    }

}
