package ch.epfl.rechor.timetable.mapped;
import java.nio.ByteBuffer;

public class CarlaStructuredBuffer {
    public static void main(String[] args) {
        testStructuredBuffer();
    }

    private static void testStructuredBuffer() {
        // Définition d'une structure fictive
        Structure structure = new Structure(new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16),
                new Structure.Field(2, Structure.FieldType.S32)); // Champs U8, U16, S32

        // Création d'un ByteBuffer contenant des données test
        ByteBuffer buffer = ByteBuffer.allocate(3 * (1 + 2 + 4));

        // Ajout des données pour trois éléments
        // Premier élément : U8 = 10, U16 = 300, S32 = -100000
        buffer.put((byte) 10);
        buffer.putShort((short) 300);
        buffer.putInt(-100000);

        // Deuxième élément : U8 = 20, U16 = 600, S32 = 200000
        buffer.put((byte) 20);
        buffer.putShort((short) 600);
        buffer.putInt(200000);

        // Troisième élément : U8 = 30, U16 = 900, S32 = -300000
        buffer.put((byte) 30);
        buffer.putShort((short) 900);
        buffer.putInt(-300000);

        // Réinitialisation de la position du buffer
        buffer.flip();

        // Création d'une instance de StructuredBuffer
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        // Vérification du nombre d'éléments
        assert structuredBuffer.size() == 3 : "Erreur: taille incorrecte";

        // Vérification des valeurs récupérées
        assert structuredBuffer.getU8(0, 0) == 10 : "Erreur sur getU8 premier élément";
        assert structuredBuffer.getU16(1, 0) == 300 : "Erreur sur getU16 premier élément";
        assert structuredBuffer.getS32(2, 0) == -100000 : "Erreur sur getS32 premier élément";

        assert structuredBuffer.getU8(0, 1) == 20 : "Erreur sur getU8 deuxième élément";
        assert structuredBuffer.getU16(1, 1) == 600 : "Erreur sur getU16 deuxième élément";
        assert structuredBuffer.getS32(2, 1) == 200000 : "Erreur sur getS32 deuxième élément";

        assert structuredBuffer.getU8(0, 2) == 30 : "Erreur sur getU8 troisième élément";
        assert structuredBuffer.getU16(1, 2) == 900 : "Erreur sur getU16 troisième élément";
        assert structuredBuffer.getS32(2, 2) == -300000 : "Erreur sur getS32 troisième élément";

        System.out.println("Tous les tests sont passés avec succès !");
    }
}