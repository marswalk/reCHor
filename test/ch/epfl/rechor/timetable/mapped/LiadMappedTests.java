package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static ch.epfl.rechor.timetable.mapped.Structure.FieldType.*;
import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static org.junit.jupiter.api.Assertions.*;

public class LiadMappedTests {
    @Test
    void testFieldWithNoFieldType(){
        assertThrows(NullPointerException.class, () -> field(1, null));
    }

    @Test
    void testSize(){
        Structure.Field field1 = field(0, Structure.FieldType.U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, Structure.FieldType.S32);
        Structure structure = new Structure(field1, field2, field3);
        assertEquals(1 + 2 + 4, structure.totalSize());
    }

    @Test
    void testInvalidIndex(){
        Structure.Field field1 = field(0, Structure.FieldType.U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, Structure.FieldType.S32);
        Structure structure = new Structure(field1, field2, field3);
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(-1, 3));
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(3, (structure.totalSize()+1)));
        assertThrows(IndexOutOfBoundsException.class, ()-> structure.offset(3, -1));
    }

    @Test
    void testUnorderedFields(){
        Structure.Field field1 = field(0, Structure.FieldType.U8);
        Structure.Field field2 = field(1, Structure.FieldType.U16);
        Structure.Field field3 = field(2, Structure.FieldType.S32);
        assertThrows(IllegalArgumentException.class, ()-> new Structure(field1, field3, field2));
    }


    @Test
    void testValidOffSet(){
        Structure.Field f1 = new Structure.Field(0, Structure.FieldType.U8);
        Structure.Field f2 = new Structure.Field(1, Structure.FieldType.U16);
        Structure structure = new Structure(f1, f2);

        assertEquals(0, structure.offset(0, 0));
        assertEquals(1, structure.offset(1, 0));
    }

    @Test
    void testSizeAL(){
        Structure.Field fieldA = field(0,U16);
        Structure.Field fieldB = field(1,U16);
        Structure.Field fieldC = field(2,U8);
        Structure.Field fieldD = field(3,S32);
        Structure structure = new Structure(fieldA,fieldB,fieldC,fieldD);
        assertEquals(9,structure.totalSize());
    }

    @Test
    void testWithNegativeIndexAL(){
        assertThrows(IllegalArgumentException.class, ()->{
            Structure.Field field = field(-2,U16);
            Structure structure = new Structure(field);
        });
    }

    @Test
    void testWithNotZeroAL(){
        assertThrows(IllegalArgumentException.class, ()->{
            Structure.Field field = field(1,U16);
            Structure structure = new Structure(field);
        });
    }

    @Test
    void testValidOffsetAL(){
        Structure.Field fieldA = new Structure.Field(0,S32);
        Structure.Field fieldB = new Structure.Field(1,U16);
        Structure.Field fieldC = new Structure.Field(2,U8);
        Structure structure = new Structure(fieldA,fieldB,fieldC);
        assertEquals(0,structure.offset(0,0));
        assertEquals(4,structure.offset(1,0));
        assertEquals(6,structure.offset(2,0));
    }


    @Test

    void platFormTest_CE() {
        HexFormat hexFormat = HexFormat.ofDelimiter(" ");
        byte[] bytes = hexFormat.parseHex("00 04 04 b6 ca 14 21 14 1f a1 00 06 04 12 21 18 da 03 01 43 00 0a d4 e8 b0 c9 ad ef 01 3B 04 41");
        // 00 0a d4 e8 = 709864 = 0.059500113129615784;
        // b0 c9 ad ef = 2966007279 = 248.6078581865876913;
        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);

        List<String> stringStationList = new ArrayList<String>(Arrays.asList("kdfsk", "efz", "1", "70", "Anet", "Ins", "Lausanne", "Losanna",
                "Palezieux", "Poutschidi", "PipiPow", "scoubididou", "dripaw",
                "20 Allée des cramtés", "12", "Skibbidi gare", "train de vie",
                "🦕", "Norman Thavaux", "PolyRat", "les fesses d'Augustin",
                "95 Avenue Apagnan", "QuoicouBoulevard",

                // Noms absurdes
                "Gare de la Cramtance", "Tunnel du Rat Crevé", "St-Honorin-les-Chips",
                "Place du Yippee", "Impasse du ScoubiSnack", "Pont du TurboFlemmard",
                "Rue de la Friteuse", "Station McFlurry", "Arrêt KFC de l’Apocalypse",
                "Bordelais-les-Saucisses", "Cul-de-sac de la Schlagance",

                // Surnoms absurdes
                "Jean-Miche-Moi", "Tonton Flingueur", "Mémé Turbo", "Le Rat Pêcheur",
                "Sauciflard le Grand", "Maître Boulard", "El Chipiron", "Gégé la Débrouille",
                "Papy Braquage", "Mamie Beretta", "Didier La Chaussette", "Kéké Nitro",
                "Tata Mojito", "Jojo les Tongs", "Bertrand Canicule",

                // Lieux absurdes
                "Rond-Point du Yolo", "Échangeur des Quoicoubeh", "Avenue du Dab",
                "Cité des Enfants Perdus (et Bourrés)", "HLM des Champions",
                "Zone Industrielle des Ratés", "Jardin Public des Dépressifs",
                "Aire de Repos 'La Sieste Éternelle'", "Boulevard du Frisson",

                // Trucs d’internet
                "LOL City", "VroumVroumVille", "Ratio Town", "NoSkillLand",
                "Mdrlavideo", "SansSoucis.exe", "CerveauLag.com", "NoName99",
                "KarmaZone", "Trollinette-sur-Loire", "Genshin-Village",
                "Osu!Land", "Fortnite-les-Bains", "Skibidi-Hall",

                // Noms inspirés de vrais endroits, remixés
                "Montparnasse-les-Piges", "RER ZZZ", "Marseille-sur-Boue",
                "Toulouse-les-Bras-Cassés", "Orléans-la-Peuf", "Lille-la-Flemme",
                "Strasbourg-dans-l'Ombre", "Gare de Saint-Malodé", "Valence-la-Goudronnée",

                // Plus de surnoms et de brainrot français
                "Titi Turbo", "Grand-Mère Crameuse", "Bébert l’Incroyable",
                "Chabichou-de-Combat", "Nico le Malin", "Dédé Dynamite",
                "Pépito Bang Bang", "Francis le Taximan", "Raoul la Grosse Patate",
                "La Mouette du 93", "Poussin Frappé", "Momo Bagarre",
                "Jacky Nitro", "Patapouf Gangsta", "Jojo Racaillou",

                // Références et délires
                "Baguettosaurus Rex", "FromageLand", "Cordon Bleu City",
                "Tacos Mégablast", "Camembert-sur-Seine", "Croque-Monsieur Beach",
                "Vin Rouge-Les-Bains", "Pain Perdu Ville", "Omelette-du-Fromage-Central",
                "GigaChèvre 3000", "Le Bifteck Mystique", "La Buvette des Enfers",

                // Ajout de brainrot complet
                "Pépin le Radis", "Clémentine l’Éclair", "Capitaine Raclette",
                "Zizou des Abysses", "Dudu le Pendu", "Kévin TurboFlex",
                "Cacahuète Sauvage", "Le Big Mac Égaré", "Tonton Cactus",
                "Papy Boomer Max", "Jean-Miche-Rienafaire", "Didou le Fourbe",
                "Les Nuggets du Turfu", "M. Mouette", "Titi Tofu",
                "Le Rat Lucide", "Grand Chef Chaussette", "L’Empereur du Picon",

                // Émojis random et brainrot maximal
                "🔥 Gare du Feu", "🌊 Plage des Vagues Fatiguées", "🦆 CoinCoin-Les-Bains",
                "🦄 Licorneville", "🐢 L’Autoroute du Speedrun", "🤡 Cirque du Brainrot",
                "🎩 Monsieur Elégance", "🍕 Pizza-les-Flots", "🚀 Fusée Turbo",
                "🌭 Hot-Dog City", "🐸 Rainette-sur-Marne", "🥖 Tradition-sur-Seine",

                // Continue jusqu'à 400...
                "Tramway du Malaise", "Panneau Stop de l’Enfer", "Raccourci des Légendes",
                "Gare des âmes en peine", "Passage des Tunnels sans Fin", "Pépèreland",
                "Métro Bizarre", "Vélo-Éclair", "Périph’ des Damnés", "Rond-Point du Doute",
                "Pont de la Chèvre Perdue", "Autoroute de la Patate Chaude",
                "Porte des Pépitos", "Bretelle de la Sieste", "Route du Nutella Déversé",

                "La Maison qui Marche", "Boulevard des Crocs Mortels", "La Rue du Pourquoi",
                "Vortex du McDo", "Parking de la Désolation", "Place du Wesh",
                "Gouffre de l’Indécision", "Allée des Fantômes Fatigués",

                "Sac à dos Sentimental", "Tartiflette Sauvage", "La Ruelle des Croissants",
                "La Forêt des Incompréhensions", "Gare de la Sauce Blanche",

                "Borne d’Arcade du Destin", "La Brasserie du Frometon",
                "Café des Âmes Perdues", "Snack du Vide Émotionnel", "Kebab des Champions",
                "GigaBuvette 2000", "Caverne du Barbecue Ultime",

                "Métro Ligne 404", "Arrêt de Bus des Mots Perdus", "TGV vers l’Inconnu",
                "Train Intergalactique", "TER de la Maldance", "Gare des Ratés",
                "Autoroute des Gens Louches", "Station des Pépettes Égarées",
                "Périphérique du Doute", "Boulevard des AirPods Perdus",
                "Route des Chaussettes Trouées", "Impasse du Croque-Monsieur",
                "Chemin des Frites Sombres", "Pont du Dropkick", "Esplanade du Poulet Frit",

                "Dédale du Wesh", "Sentier du Jean-Michel", "Station WTF", "Rond-Point de l’Absurde",


                // Stations du Chaos
                "Gare de la Maldance", "Station des Âmes Perdues", "Tunnel du Fond du Gouffre",
                "Impasse de la Dernière Chance", "Route du Zbeul Infini", "Chemin de Traverse des Ratés",
                "Station Sans Issue", "RER du Malaise", "Avenue de la Sauce Blanche",
                "Autoroute des Clowns Tristes", "Bretelle de l’Incompréhension",
                "Station du Débat Éternel", "Arrêt de Bus de la Réflexion Tardive",

                // Rues et avenues de l'absurde
                "Rue des Crocs Morts", "Avenue du Turfu Sombre", "Boulevard du Croissant Dépressif",
                "Impasse des Clowns Fatigués", "Rond-Point du PLS", "Pont de la Mélancolie Joyeuse",
                "Allée des Pépettes Perdues", "Rue de la Chèvre Foudroyée",
                "Chemin du Rat Lucide", "Rond-Point des Pépitos Tombés",

                // Surnoms absurdes & personnages mystiques
                "Jojo Sauciflard", "Didier PLS", "Raoul le Turbo", "Jean-Michel Désastre",
                "Bernard Sauce Blanche", "Tonton Fumier", "Jacky Deux Vitesses",
                "Pépé Traquenard", "Mamie Javel", "Francis la Peur",
                "Kéké la Détresse", "Dudu la Semelle", "Chico Maléfique",
                "Jean-Kévin le Déçu", "Pépito Gargantua", "Momo Charcuterie",
                "Serge la Frite", "Gégé TurboFlemme", "Patrick Désintégré",

                // Lieux du Chaos
                "Snack du Vide Émotionnel", "Cimetière des Croissants Perdus",
                "Bistrot du Fromage Déchu", "Brasserie du Grand Déni",
                "La Buvette des Âmes Sèches", "Kebab du Dernier Espoir",
                "McDo du Jugement Dernier", "Café de la Giga-Raclée",
                "Brasserie des Pépis", "Auberge des Champions Déchus",

                // Brainrot ultime
                "Forteresse du Zbeul", "Tour de la Maldance", "Citadelle du Yippee",
                "Dédale du Jean-Michel", "Labyrinthe du Scoubididou",
                "Cave des Croutons", "Donjon de la Raclette",
                "Plage des Cornichons Tristes", "Volcan de la Défaite",

                // Émojis et absurdités visuelles
                "🦆 CoinCoin-Plage", "🔥 Gare du Braquage", "💀 Rond-Point du Non-Retour",
                "🛑 Stop-les-Dégâts", "🚀 Fusée TurboFlex", "🎭 Théâtre du Scandale",
                "🥖 Tradiland", "🧀 Camembert-Sur-Terre", "🥤 Fontaine de l'Orangina Sacré",
                "📉 Cours du Bitcoin Effondré", "👀 Station du Regard Pesant",

                // Noms de bouffe remixés
                "Pôle Nord du Tacos", "Antarctique du Kebab", "Archipel du Gratin Dauphinois",
                "Frontière du McBaguette", "Lac du Nutella Renversé",
                "Île du Croque-Monsieur Perdu", "Toundra du Camembert",
                "Désert du Curry-Dinde", "Pic de la Fondue Noire", "Vallée du Big Mac Sacré",

                // Transports de la Maldance
                "Métro Ligne 666", "Tramway de la Faucheuse", "Funiculaire du PLS",
                "TGV de l'Angoisse", "Train de la Nuit Sans Fin", "Navette Spatiale du Doute",
                "Bus de la Déchéance", "Scooter des Enfers", "Péniche du Mal Aigu",
                "Chemin de Fer des Fesses d'Augustin",

                // Délire complet
                "Temple des Rats Perchés", "Carrefour des Énergies Basses",
                "Hall du Burnout", "Stade des Petits Bras", "Parking du Grand Non",
                "Bibliothèque des TikTok Perdus", "Musée du Dernier Mouvement",
                "Grotte du Dernier Fromage", "Marécage du Malaise Chronique",
                "Pâtisserie des Âmes Errantes",

                // Titre et surnoms de champions du malaise
                "Baron de la Chaussette Solitaire", "Empereur du Pain Rassi",
                "Duc de la Flemme Universelle", "Seigneur du Jambon Perdu",
                "Comte du Saucisson Disparu", "Général du Plateau de Fromage",
                "Archiduc de la Ratitude", "Prince du Dernier Noyau d’Olive",

                // Encore plus de lieux absurdes
                "Forêt des Cornichons Mystiques", "Canyon des Peignoirs Fatigués",
                "Jungle des Tupperwares Perdus", "Désert des Gens qui Promettent de Rappeler",
                "Île des Codes Wifi Disparus", "Lac du Silure Sacré",
                "Mer du Gras Infinie", "Péninsule de la Chaise Qui Grince",

                // Derniers étages du brainrot
                "Sommet du Slip Perdu", "Crique des Rayures Inutiles",
                "Tunnel des Talons Qui Claquent", "Autoroute du Ralenti",
                "Pont des Pavés Mouillés", "Immeuble des Gens Qui Parlent Trop Fort",
                "Rivière du Mauvais Timing", "Bois du SMS Non Répondu",

                // Bonus TurboGoofy
                "Gare des Portes Qui Coincent", "Station de l'Imprimante Révoltée",
                "Terminal des Ongles Cassés", "Aire de Repos des Chaussures Mal Lacées",
                "Rond-Point de l’Allergie Surprise", "Kebab de l’Hésitation",
                "Snack de l’Inconnu", "Pôle Emploi du Fayotage Raté",
                "Grotte du Silence Pesant", "Jardin des Barbecue Maudits"));


        //Immeuble des Gens Qui Parlent Trop Fort = 315;
        BufferedPlatforms platforms = new BufferedPlatforms(stringStationList, bytesBuffer);
        assertEquals(platforms.name(7),stringStationList.get(315));
        assertEquals(platforms.stationId(7),1089 );
    }
}
