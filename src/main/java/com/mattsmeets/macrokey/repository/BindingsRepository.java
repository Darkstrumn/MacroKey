package com.mattsmeets.macrokey.repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mattsmeets.macrokey.MacroKey;
import com.mattsmeets.macrokey.model.*;
import com.mattsmeets.macrokey.service.JsonConfig;

/**
 * Repository class for the bindings.json file
 */
public class BindingsRepository {

    /**
     * JsonConfig helper class
     */
    private final JsonConfig config;

    /**
     * File template used for serializing data into bindings.json
     */
    private BindingsFileInterface bindingsFile;

    /**
     * Initialisation of the repository
     * Note:
     * The repository will automatically
     * sync when initialized
     *
     * @param jsonConfig the JsonConfig helper
     * @throws IOException when the file can not be found or modified
     */
    public BindingsRepository(JsonConfig jsonConfig) throws IOException {
        this.config = jsonConfig;

        loadConfiguration();
    }

    /**
     * Set the used bindings file.
     *
     * @param bindingsFile instance of BindingsFile
     */
    public void setBindingsFile(BindingsFileInterface bindingsFile) {
        this.bindingsFile = bindingsFile;
    }

    /**
     * Find all layers
     *
     * @param sync update from file before retrieving all layers
     * @return list of all layers
     * @throws IOException when file can not be found or read
     */
    public Set<LayerInterface> findAllLayers(boolean sync) throws IOException {
        if (sync)
            // if specified to update memory with latest changes
            loadConfiguration();

        return this.bindingsFile.getLayers();
    }

    /**
     * Find layer by UUID
     *
     * @param ulid UUID
     * @param sync boolean update from file before retrieving
     * @return the layer found, may be null
     * @throws IOException when file can not be found or read
     */
    public LayerInterface findLayerByUUID(UUID ulid, boolean sync) throws IOException {
        if (sync)
            // if specified to update memory with latest changes
            loadConfiguration();

        return this.bindingsFile
                .getLayers()
                .stream()
                .filter(
                        (layer) ->
                                layer.getULID() == ulid
                )
                .reduce((u, v) -> {
                    throw new IllegalStateException("More than one ID found");
                })
                .orElse(null);
    }

    /**
     * Add Layder
     *
     * @param layer affected layer
     * @param sync  update file after adding layer
     * @throws IOException when file can not be found or read
     */
    public void addLayer(LayerInterface layer, boolean sync) throws IOException {
        this.bindingsFile.addLayer(layer);

        if (sync) {
            // if specified to update configuration
            saveConfiguration();
        }
    }

    /**
     * Edit Layer
     *
     * @param layer affected layer
     * @param sync  update file after updating layer
     * @throws IOException when file can not be found or read
     */
    public void updateLayer(LayerInterface layer, boolean sync) throws IOException {
        this.bindingsFile.setLayers(
                // get all layer's and go through all of them
                // when the ULID matches with the given layer
                // then return the given layer instead of the
                // layer that is currently being iterated over.
                // finally collect results into a Set<Layer>
                this.bindingsFile
                        .getLayers()
                        .stream()
                        .map(savedLayer -> layer.getULID() == savedLayer.getULID() ? layer : savedLayer)
                        .collect(Collectors.toSet())
        );

        if (sync) {
            // if specified to update configuration
            saveConfiguration();
        }
    }

    /**
     * Remove Layer by UUID
     *
     * @param ulid the unique layer identifier of the affected layer
     * @param sync update file after adding macro
     * @throws IOException when file can not be found or read
     */
    public void deleteLayer(UUID ulid, boolean sync) throws IOException {
        this.bindingsFile.setLayers(
                // get all layer's and filter them
                // when the ulid of the layer matches
                // the given ulid, then we will filter
                // that out of the result set
                this.bindingsFile
                        .getLayers()
                        .stream()
                        .filter(savedLayer -> ulid.compareTo(savedLayer.getULID()) != 0)
                        .collect(Collectors.toSet())
        );

        if (sync) {
            // if specified to update configuration
            saveConfiguration();
        }
    }

    /**
     * Remove Layer by instance
     *
     * @param layer the affected layer
     * @param sync  update file after removing layer
     * @throws IOException when file can not be found or read
     */
    public void deleteLayer(LayerInterface layer, boolean sync) throws IOException {
        this.deleteLayer(layer.getULID(), sync);
    }

    /**
     * Find all active macro's
     *
     * @param sync update from file before retrieving all macros
     * @return list of all macros
     * @throws IOException when file can not be found or read
     */
    public Set<MacroInterface> findAllMacros(boolean sync) throws IOException {
        if (sync)
            // if specified to update memory with latest changes
            loadConfiguration();

        return this.bindingsFile.getMacros();
    }

    public boolean isMacroInLayer(MacroInterface macro, LayerInterface layer) {
        return layer.getMacros().contains(macro.getUMID());
    }

    /**
     * Find active macro by its ULID
     *
     * @param ulid the macro's ulid
     * @param sync update from file before retrieving all macros
     * @return list of active macro's with the given keyCode as trigger
     * @throws IOException when file can not be found or read
     */
    public MacroInterface findMacroByUUID(UUID ulid, boolean sync) throws IOException {
        if (sync)
            // if specified to update memory with latest changes
            loadConfiguration();

        // get all macros and filter through them
        // searching for entries that have the given
        // keyCode, and are active; finally collect
        // the results into a Set<Macro>.
        return this.bindingsFile
                .getMacros()
                .stream()
                .filter(
                        (macro) ->
                                macro.getUMID() == ulid
                )
                .reduce((u, v) -> {
                    throw new IllegalStateException("More than one ID found");
                })
                .orElse(null);
    }

    /**
     * Find active macro's by its keycode
     *
     * @param keyCode uses Keyboard keyCode
     * @param sync    update from file before retrieving all macros
     * @return list of active macro's with the given keyCode as trigger
     * @throws IOException when file can not be found or read
     */
    public Set<MacroInterface> findMacroByKeycode(int keyCode, LayerInterface layer, boolean sync) throws IOException {
        if (sync)
            // if specified to update memory with latest changes
            loadConfiguration();

        // get all macros and filter through them
        // searching for entries that have the given
        // keyCode, and are active; it then checks
        // if the layer is null, or the macro exists
        // in the current layer. finally collect
        // the results into a Set<Macro>.
        return this.bindingsFile
                .getMacros()
                .stream()
                .filter(
                        (macro) ->
                                macro.getKeyCode() == keyCode
                                        && macro.isActive()
                                        && (layer == null || isMacroInLayer(macro, layer))
                )
                .collect(Collectors.toSet());
    }

    /**
     * Add Macro
     *
     * @param macro affected macro
     * @param sync  update file after adding macro
     * @throws IOException when file can not be found or read
     */
    public void addMacro(MacroInterface macro, boolean sync) throws IOException {
        this.bindingsFile.addMacro(macro);

        if (sync) {
            // if specified to update configuration
            saveConfiguration();
        }
    }

    /**
     * Edit Macro
     *
     * @param macro affected macro
     * @param sync  update file after adding macro
     * @throws IOException when file can not be found or read
     */
    public void updateMacro(MacroInterface macro, boolean sync) throws IOException {
        this.bindingsFile.setMacros(
                // get all macro's and go through all of them
                // when the UMID matches with the given macro
                // then return the given macro instead of the
                // macro that is currently being iterated over.
                // finally collect results into a Set<Macro>
                this.bindingsFile
                        .getMacros()
                        .stream()
                        .map(savedMacro -> macro.getUMID() == savedMacro.getUMID() ? macro : savedMacro)
                        .collect(Collectors.toSet())
        );

        if (sync) {
            // if specified to update configuration
            saveConfiguration();
        }
    }

    /**
     * Remove Macro by UUID
     *
     * @param umid the unique macro identifier of the affected macro
     * @param sync update file after adding macro
     * @throws IOException when file can not be found or read
     */
    public void deleteMacro(UUID umid, boolean sync) throws IOException {
        this.bindingsFile.setMacros(
                // get all macro's and filter them
                // when the umid of the macro matches
                // the given umid, then we will filter
                // that out of the result set
                this.bindingsFile
                        .getMacros()
                        .stream()
                        .filter(savedMacro -> umid.compareTo(savedMacro.getUMID()) != 0)
                        .collect(Collectors.toSet())
        );

        if (sync) {
            // if specified to update configuration
            saveConfiguration();
        }
    }

    /**
     * Remove Macro by instance
     *
     * @param macro the affected macro
     * @param sync  update file after adding macro
     * @throws IOException when file can not be found or read
     */
    public void deleteMacro(MacroInterface macro, boolean sync) throws IOException {
        this.deleteMacro(macro.getUMID(), sync);
    }

    /**
     * Get file configuration version
     *
     * @return will return the value of the "version" key
     */
    public int findFileVersion() {
        return this.bindingsFile.getVersion();
    }

    /**
     * Save the current BindingFile to json
     *
     * @throws IOException when file can not be found or read
     */
    public void saveConfiguration() throws IOException {
        this.config.saveObjectToJson(this.bindingsFile);
    }

    /**
     * Loads the json file into memory
     *
     * @throws IOException when file can not be found or read
     */
    public void loadConfiguration() throws IOException {
        JsonObject jsonObject = this.config.getJSONObject();

        if (jsonObject != null) {
            // on initialization the bindingsFile will not be set.
            if (this.bindingsFile == null) {
                setBindingsFile(new BindingsFile(jsonObject.get("version").getAsInt()));
            }

            // retrieve all macro's from the bindings.json file
            // and add them inside our bindingsFile
            MacroInterface[] macroArray = this.config.bindJsonElementToObject(Macro[].class, jsonObject.get("macros"));
            this.bindingsFile
                    .setMacros(Arrays
                            .stream(macroArray)
                            .collect(Collectors.toSet())
                    );

            LayerInterface[] layerArray = this.config.bindJsonElementToObject(Layer[].class, jsonObject.get("layers"));
            this.bindingsFile
                    .setLayers(Arrays
                            .stream(layerArray)
                            .collect(Collectors.toSet()));
        } else {
            // the bindings.json has just been created, or
            // has been corrupted. We will just create a fresh
            // installation to prevent errors.
            setBindingsFile(new BindingsFile(2, new HashSet<>()));

            saveConfiguration();
        }
    }
}
