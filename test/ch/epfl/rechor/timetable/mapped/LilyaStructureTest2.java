package ch.epfl.rechor.timetable.mapped;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class LilyaStructureTest2 {

    @Test
    void testTotalSizeAndOffset() {
        // Création d'une structure avec deux champs de type U16 (2 octets chacun)
        Structure.Field f0 = Structure.field(0, Structure.FieldType.U16);
        Structure.Field f1 = Structure.field(1, Structure.FieldType.U16);
        Structure structure = new Structure(f0, f1);

        // La taille totale attendue est 2 + 2 = 4
        assertEquals(4, structure.totalSize(), "La taille totale devrait être 4 octets.");

        // Les offsets attendus :
        // f0 : offset = 0
        // f1 : offset = 2 (puisque f0 occupe 2 octets)
        // Pour l'élément d'index 1, offset(f1,1) = 2 + 1*4 = 6
        assertEquals(0, structure.offset(0, 0), "Offset f0 pour l'élément 0 doit être 0.");
        assertEquals(2, structure.offset(1, 0), "Offset f1 pour l'élément 0 doit être 2.");
        assertEquals(4, structure.offset(0, 1), "Offset f0 pour l'élément 1 doit être 4.");
        assertEquals(6, structure.offset(1, 1), "Offset f1 pour l'élément 1 doit être 6.");
    }

    @Test
    void testFieldOrderValidation() {
        // Vérifie que la construction échoue si les champs ne sont pas dans l'ordre attendu.
        // Ici, on crée d'abord un champ d'index 1 puis un champ d'index 0.
        Structure.Field f1 = Structure.field(1, Structure.FieldType.U8);
        Structure.Field f0 = Structure.field(0, Structure.FieldType.U16);

        assertThrows(IllegalArgumentException.class, () -> new Structure(f1, f0));
    }

    @Test
    void testNullFieldType() {
        // Vérifie qu'un champ créé avec un type null lève bien une NullPointerException.
        assertThrows(NullPointerException.class, () -> new Structure.Field(0, null)); // On instancie un Field de type null
    }



    @Test
    void testDifferentFieldTypes() {
        // Création d'une structure avec trois champs de types différents : U8 (1 octet), U16 (2 octets), S32 (4 octets)
        Structure.Field f0 = Structure.field(0, Structure.FieldType.U8);   // 1 octet
        Structure.Field f1 = Structure.field(1, Structure.FieldType.U16);  // 2 octets
        Structure.Field f2 = Structure.field(2, Structure.FieldType.S32);  // 4 octets
        Structure structure = new Structure(f0, f1, f2);

        int expectedTotalSize = 1 + 2 + 4; // 7 octets
        assertEquals(expectedTotalSize, structure.totalSize(), "La taille totale devrait être 7 octets.");

        // Offsets attendus :
        // f0 : 0
        // f1 : 1 (après 1 octet de f0)
        // f2 : 1 + 2 = 3
        assertEquals(0, structure.offset(0, 0), "Offset f0 pour l'élément 0 doit être 0.");
        assertEquals(1, structure.offset(1, 0), "Offset f1 pour l'élément 0 doit être 1.");
        assertEquals(3, structure.offset(2, 0), "Offset f2 pour l'élément 0 doit être 3.");

        // Pour un enregistrement d'index 2, l'offset pour f2 devrait être : 3 + 2*7 = 17
        assertEquals(17, structure.offset(2, 2), "Offset f2 pour l'élément 2 doit être 17.");
    }
}
