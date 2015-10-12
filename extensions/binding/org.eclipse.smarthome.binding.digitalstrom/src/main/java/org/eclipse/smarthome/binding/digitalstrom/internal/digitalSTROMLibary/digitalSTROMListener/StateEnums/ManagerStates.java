package org.eclipse.smarthome.binding.digitalstrom.internal.digitalSTROMLibary.digitalSTROMListener.StateEnums;

/**
 * The {@link ManagerStates} contains all reachable states of the digitalSTROMManager.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public enum ManagerStates {

    running,
    stopped,
    initialasing,
    generatingScenes,
    scenesGenerated;
}
