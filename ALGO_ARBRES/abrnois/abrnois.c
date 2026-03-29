#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define TAILLE_MOT 128

/**
 * Structure d'un noeud d'ABRnois.
 * mot : mot stocké
 * nb_occ : nombre d'occurrences (priorité)
 * fg, fd : fils gauche et droit
 */
typedef struct _noeud {
    char *mot;
    int nb_occ;
    struct _noeud *fg, *fd;
} Noeud, *ABRnois;

/**
 * Cellule pour liste chaînée de Noeud.
 */
typedef struct _cell {
    Noeud *n;
    struct _cell *suivant;
} Cell, *Liste;

/**
 * Structure temporaire pour stocker mot et nb_occ lors du tri/affichage.
 */
typedef struct {
    char *mot;
    int nb_occ;
} MotFreq;

/**
 * Alloue un nouveau noeud pour l'arbre ABRnois avec le mot donné.
 * L'occurrence est initialisée à 1.
 * Retourne un pointeur vers le noeud nouvellement alloué, ou NULL en cas d'échec.
 */
Noeud *alloue_noeud(char *mot) {
    Noeud *nouveau = (Noeud *)malloc(sizeof(Noeud));
    if (!nouveau) return NULL;
    nouveau->mot = strdup(mot);
    nouveau->nb_occ = 1;
    nouveau->fg = NULL;
    nouveau->fd = NULL;
    return nouveau;
}

/**
 * Nettoie un mot en ne gardant que les lettres alphabétiques en minuscules.
 * Modifie la chaîne en place.
 */
void nettoie_mot(char *mot) {
    int i = 0, j = 0;
    while (mot[i]) {
        if (isalpha((unsigned char)mot[i]))
            mot[j++] = tolower((unsigned char)mot[i]);
        i++;
    }
    mot[j] = '\0';
}

/**
 * Effectue une rotation gauche sur l'arbre *A si l'enfant droit existe.
 */
void rotation_gauche(ABRnois *A) {
    if (*A == NULL || (*A)->fd == NULL) return;
    Noeud *temp = (*A)->fd;
    (*A)->fd = temp->fg;
    temp->fg = *A;
    *A = temp;
}

/**
 * Effectue une rotation droite sur l'arbre *A si l'enfant gauche existe.
 */
void rotation_droite(ABRnois *A) {
    if (*A == NULL || (*A)->fg == NULL) return;
    Noeud *temp = (*A)->fg;
    (*A)->fg = temp->fd;
    temp->fd = *A;
    *A = temp;
}

/**
 * Insère un mot dans l'arbre *A.
 * Si le mot existe déjà, incrémente son occurrence.
 * Retourne 1 si succès, 0 si échec d'allocation.
 */
int insert_ABRnois(ABRnois *A, char *mot) {
    if (*A == NULL) {
        *A = alloue_noeud(mot);
        return (*A != NULL) ? 1 : 0;
    }
    int cmp = strcmp(mot, (*A)->mot);
    if (cmp == 0) {
        (*A)->nb_occ++;
        return 1;
    } else if (cmp < 0) {
        return insert_ABRnois(&((*A)->fg), mot);
    } else {
        return insert_ABRnois(&((*A)->fd), mot);
    }
}

/**
 * Insère un noeud (copié) dans une liste triée alphabétiquement.
 */
void insere_liste_alpha(Liste *lst, Noeud *n) {
    Noeud *copie = malloc(sizeof(Noeud));
    if (!copie) return;
    copie->mot = strdup(n->mot);
    copie->nb_occ = n->nb_occ;
    copie->fg = NULL;
    copie->fd = NULL;

    Cell *nv = malloc(sizeof(Cell));
    if (!nv) {
        free(copie->mot);
        free(copie);
        return;
    }
    nv->n = copie;
    Cell **p = lst;
    while (*p && strcmp(copie->mot, (*p)->n->mot) > 0) p = &((*p)->suivant);
    nv->suivant = *p;
    *p = nv;
}

/**
 * Supprime tous les noeuds de l'arbre *A ayant nb_occ == max_occ,
 * les ajoute à la liste *lst (triée alphabétiquement).
 * Retourne le nombre de noeuds supprimés.
 */
int supprime_noeud_max(ABRnois *A, int max_occ, Liste *lst) {
    if (*A == NULL) return 0;
    int nb = 0;
    if ((*A)->nb_occ == max_occ) {
        insere_liste_alpha(lst, *A);
        // Suppression du nœud
        ABRnois tmp = *A;
        if (!tmp->fg && !tmp->fd) {
            *A = NULL;
        } else if (!tmp->fg) {
            *A = tmp->fd;
        } else if (!tmp->fd) {
            *A = tmp->fg;
        } else {
            // Remplacement par le successeur le plus à gauche du sous-arbre droit
            ABRnois *succ = &tmp->fd;
            while ((*succ)->fg) succ = &((*succ)->fg);
            Noeud *remp = *succ;
            tmp->mot = remp->mot;
            tmp->nb_occ = remp->nb_occ;
            // On supprime le successeur
            *succ = remp->fd;
            free(remp);
        }
        return 1;
    }
    nb += supprime_noeud_max(&((*A)->fg), max_occ, lst);
    nb += supprime_noeud_max(&((*A)->fd), max_occ, lst);
    return nb;
}

/**
 * Extrait tous les noeuds de l'arbre *A ayant la priorité maximale (nb_occ de la racine).
 * Les ajoute à la liste *lst (triée alphabétiquement).
 * Retourne le nombre de noeuds extraits.
 */
int extrait_priorite_max(ABRnois *A, Liste *lst) {
    if (*A == NULL) return 0;
    int max_occ = (*A)->nb_occ;
    *lst = NULL;
    int nb = supprime_noeud_max(A, max_occ, lst);
    return nb;
}

/**
 * Fusionne les cellules de la liste *lst ayant le même mot (somme les occurrences).
 */
void fusionne_liste(Liste *lst) {
    for (Cell *c1 = *lst; c1; c1 = c1->suivant) {
        Cell *prev = c1;
        Cell *c2 = c1->suivant;
        while (c2) {
            if (strcmp(c1->n->mot, c2->n->mot) == 0) {
                c1->n->nb_occ += c2->n->nb_occ;
                prev->suivant = c2->suivant;
                free(c2->n->mot);
                free(c2->n);
                free(c2);
                c2 = prev->suivant;
            } else {
                prev = c2;
                c2 = c2->suivant;
            }
        }
    }
}

/**
 * Libère toute la mémoire de l'arbre A.
 */
void libere_arbre(ABRnois A) {
    if (A == NULL) return;
    libere_arbre(A->fg);
    libere_arbre(A->fd);
    free(A->mot);
    free(A);
}

/**
 * Libère toute la mémoire de la liste l.
 */
void libere_liste(Liste l) {
    while (l) {
        Cell *tmp = l;
        l = l->suivant;
        free(tmp->n->mot);
        free(tmp->n);
        free(tmp);
    }
}

/**
 * Affiche l'usage du programme.
 */
void usage(char *prog) {
    printf("Usage: %s frequents.txt [-g] [-n p] corpus1.txt [corpus2.txt ...]\n", prog);
}

/**
 * Calcule le total des occurrences de tous les mots de l'arbre a.
 */
void parcours_total(ABRnois a, int *total) {
    if (!a) return;
    *total += a->nb_occ;
    parcours_total(a->fg, total);
    parcours_total(a->fd, total);
}

/**
 * Fonction de comparaison pour qsort : trie par nb_occ décroissant, puis alphabétique.
 */
int cmp(const void *a, const void *b) {
    const MotFreq *ma = a, *mb = b;
    if (mb->nb_occ != ma->nb_occ)
        return mb->nb_occ - ma->nb_occ;
    return strcmp(ma->mot, mb->mot);
}

/**
 * Extrait les mots les plus fréquents de l'arbre *arbre et les écrit dans out.
 * Si opt_n est activé, limite à n_max mots (en incluant ceux à égalité).
 * Les mots sont triés par fréquence décroissante puis alphabétique.
 * total_occ sert au calcul du pourcentage.
 */
void traite_extraction(ABRnois *arbre, FILE *out, int total_occ, int opt_g, int opt_n, int n_max) {
    MotFreq *tab = NULL;
    int cap = 16, n = 0;
    tab = malloc(cap * sizeof(MotFreq));
    Liste lst = NULL;
    int extrait = 0;
    int last_occ = -1;
    int stop = 0;

    while (*arbre && !stop) {
        lst = NULL;
        int nb = extrait_priorite_max(arbre, &lst);
        if (nb == 0) break;
        fusionne_liste(&lst);
        for (Cell *c = lst; c; c = c->suivant) {
            if (n == cap) {
                cap *= 2;
                tab = realloc(tab, cap * sizeof(MotFreq));
            }
            tab[n].mot = strdup(c->n->mot);
            tab[n].nb_occ = c->n->nb_occ;
            n++;
            extrait++;
        }
        if (opt_n && extrait >= n_max) {
            last_occ = tab[n-1].nb_occ;
            // On continue à extraire tant que les mots ont la même occurrence que le dernier extrait
            while (*arbre) {
                Liste lst2 = NULL;
                int nb2 = extrait_priorite_max(arbre, &lst2);
                if (nb2 == 0) break;
                fusionne_liste(&lst2);
                int found_same = 0;
                for (Cell *c2 = lst2; c2; c2 = c2->suivant) {
                    if (c2->n->nb_occ == last_occ) {
                        if (n == cap) {
                            cap *= 2;
                            tab = realloc(tab, cap * sizeof(MotFreq));
                        }
                        tab[n].mot = strdup(c2->n->mot);
                        tab[n].nb_occ = c2->n->nb_occ;
                        n++;
                        extrait++;
                        found_same = 1;
                    }
                }
                libere_liste(lst2);
                if (!found_same) break;
            }
            stop = 1;
        }
        libere_liste(lst);
    }
    // Tri décroissant par nb_occ, puis alphabétique
    qsort(tab, n, sizeof(MotFreq), cmp);
    for (int i = 0; i < n; i++) {
        double freq = 100.0 * tab[i].nb_occ / total_occ;
        fprintf(out, "%s %.2f%%\n", tab[i].mot, freq);
        free(tab[i].mot);
    }
    free(tab);
}

/**
 * Exporte l'arbre A au format PDF (nommé nom_pdf) via Graphviz/dot.
 * Retourne 1 si succès, 0 sinon.
 */
int exporte_arbre(char *nom_pdf, ABRnois A) {
    FILE *dot = fopen("arbre.dot", "w");
    if (!dot) {
        fprintf(stderr, "Erreur ouverture fichier arbre.dot\n");
        return 0;
    }
    fprintf(dot, "digraph G {\n");
    ABRnois pile[1024];
    int sp = 0;
    if (A) pile[sp++] = A;
    while (sp) {
        ABRnois a = pile[--sp];
        if (a->fg) {
            fprintf(dot, "    \"%s (%d)\" -> \"%s (%d)\";\n", a->mot, a->nb_occ, a->fg->mot, a->fg->nb_occ);
            pile[sp++] = a->fg;
        }
        if (a->fd) {
            fprintf(dot, "    \"%s (%d)\" -> \"%s (%d)\";\n", a->mot, a->nb_occ, a->fd->mot, a->fd->nb_occ);
            pile[sp++] = a->fd;
        }
    }
    fprintf(dot, "}\n");
    fclose(dot);

    char cmd[256];
    snprintf(cmd, sizeof(cmd), "dot -Tpdf arbre.dot -o %s", nom_pdf);
    if (system(cmd) != 0) {
        fprintf(stderr, "Erreur lors de la génération du PDF\n");
        return 0;
    }
    snprintf(cmd, sizeof(cmd), "start %s", nom_pdf);
    system(cmd);
    return 1;
}

/**
 * Fonction principale : gère les options, lit les fichiers corpus, construit l'arbre,
 * effectue les extractions et écrit le résultat dans le fichier de sortie.
 */
int main(int argc, char *argv[]) {
    if (argc < 3) {
        usage(argv[0]);
        return 1;
    }

    char *fichier_frequents = argv[1];
    int opt_g = 0, opt_n = 0, n_max = 0;
    int argi = 2;

    while (argi < argc && argv[argi][0] == '-') {
        if (strcmp(argv[argi], "-g") == 0) {
            opt_g = 1;
            argi++;
        } else if (strcmp(argv[argi], "-n") == 0 && argi + 1 < argc) {
            opt_n = 1;
            n_max = atoi(argv[argi + 1]);
            argi += 2;
        } else {
            usage(argv[0]);
            return 1;
        }
    }

    if (argi >= argc) {
        usage(argv[0]);
        return 1;
    }

    ABRnois arbre = NULL;

    int insertion_count = 0;
    for (int i = argi; i < argc; i++) {
        FILE *f = fopen(argv[i], "r");
        if (!f) {
            fprintf(stderr, "Erreur ouverture fichier %s\n", argv[i]);
            continue;
        }
        char mot[TAILLE_MOT];
        while (fscanf(f, "%127s", mot) == 1) {
            nettoie_mot(mot);
            if (mot[0] == '\0') continue;
            insert_ABRnois(&arbre, mot);
            if (opt_g) {
                char pdfname[64];
                snprintf(pdfname, sizeof(pdfname), "insertion_%d.pdf", ++insertion_count);
                exporte_arbre(pdfname, arbre);
            }
        }
        fclose(f);
    }

    int total_occ = 0;
    parcours_total(arbre, &total_occ);

    FILE *out = fopen(fichier_frequents, "w");
    if (!out) {
        fprintf(stderr, "Erreur ouverture fichier %s\n", fichier_frequents);
        libere_arbre(arbre);
        return 1;
    }

    if (opt_g) {
        exporte_arbre("arbre.pdf", arbre);
    }

    traite_extraction(&arbre, out, total_occ, opt_g, opt_n, n_max);

    fclose(out);
    libere_arbre(arbre);
    return 0;
}
