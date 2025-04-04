package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of TimeTable interface that provides access to timetable data
 * stored in flattened format files.
 * <p>
 * The class represents a public transport timetable with data stored in separate files
 * within a directory. It loads and maps data into memory-mapped byte buffers for efficient access.
 * <p>
 * The timetable contains data for stations, station aliases, platforms, routes, transfers,
 * and date-specific trips and connections.
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public record FileTimeTable(
        Path directory,
        List<String> stringTable,
        Stations stations,
        StationAliases stationAliases,
        Platforms platforms,
        Routes routes,
        Transfers transfers) implements TimeTable {

    /**
     * Creates a new FileTimeTable instance from a directory containing timetable data files.
     *
     * @param directory the path to the directory containing timetable data files
     * @return a new FileTimeTable instance
     * @throws IOException if can't read file
     */
    public static TimeTable in(Path directory) throws IOException {
        // Read string table (using ISO 8859-1 encoding as specified)
        Path strings = directory.resolve("strings.txt");
        List<String> stringTable = List.copyOf(Files.readAllLines(strings, StandardCharsets.ISO_8859_1));

        // Map static data files into memory
        ByteBuffer stationsBuffer = map(directory.resolve("stations.bin"));
        ByteBuffer stationAliasesBuffer = map(directory.resolve("station-aliases.bin"));
        ByteBuffer platformsBuffer = map(directory.resolve("platforms.bin"));
        ByteBuffer routesBuffer = map(directory.resolve("routes.bin"));
        ByteBuffer transfersBuffer = map(directory.resolve("transfers.bin"));

        // Create buffered instances
        Stations stations = new BufferedStations(stringTable, stationsBuffer);
        StationAliases stationAliases = new BufferedStationAliases(stringTable, stationAliasesBuffer);
        Platforms platforms = new BufferedPlatforms(stringTable, platformsBuffer);
        Routes routes = new BufferedRoutes(stringTable, routesBuffer);
        Transfers transfers = new BufferedTransfers(transfersBuffer);

        return new FileTimeTable(
                directory,
                stringTable,
                stations,
                stationAliases,
                platforms,
                routes,
                transfers);
    }

    /**
     * Maps a file into memory using memory-mapped byte buffer.
     *
     * @param path the path to the file to map
     * @return a ByteBuffer containing the file data
     * @throws IOException if an I/O error occurs
     */
    private static ByteBuffer map(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stations stations() {
        return stations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Platforms platforms() {
        return platforms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Routes routes() {
        return routes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transfers transfers() {
        return transfers;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UncheckedIOException if an I/O error occurs while mapping file
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        Path dateFolder = directory.resolve(date.toString());
        Path tripsPath = dateFolder.resolve("trips.bin");

        try {
            ByteBuffer tripsBuffer = map(tripsPath);
            return new BufferedTrips(stringTable, tripsBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws UncheckedIOException if an I/O error occurs while mapping files
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        Path dateFolder = directory.resolve(date.toString());
        Path connectionsPath = dateFolder.resolve("connections.bin");
        Path succPath = dateFolder.resolve("connections-succ.bin");

        try {
            ByteBuffer connectionsBuffer = map(connectionsPath);
            ByteBuffer succBuffer = map(succPath);
            return new BufferedConnections(connectionsBuffer, succBuffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}