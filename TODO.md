### TO FIX:
- "Set home apps" pulsante sui settings per scegliere le app sulla home screen (lista delle app correntemente sulla home che posso riordinarle ed eliminare (magari con uno swipe dalla lista?) e pulsante in basso "Add app" che mostra una lista di tutte le app app installate, con una barra di ricerca, clicco su una e la aggiunge alla lista precedente)

- [ ] problema con il swipe to dismiss quando dentro il drawer
    - [X] la velocity per dismiss non viene calcolata bene penso perche' usa le coordinate locali e nel frattempo il drawer scorre giu' quindi si messano up
    - [ ] quando sono a meta' lista e scrollo verso l'alto e provo a dismissare non ci riesco ma c'e' un piccolo delay (controllare come viene impostato il flag isAtTop)

- se la navbar e' visibile e' sopra il contenuto dello schermo. Invece i layout delle attivita' dovrebbero essere ridimensionati per non occupare tutto lo schermo

- organizza gli import nei file delle activity, vedi quali eliminare e ordinare




### TO IMPLEMENT:
- nei settings ma in generale per tutta l'app, invece di usare lo standard di android di mettere in alto a sinistra una freccia per tornare indietro (backbutton), mettere la navigazione in basso, piu' accessibile dalle dita, quindi ad esempio posso mettere un pulsante orizzontale tutta la width dello schermo, con la freccia verso sinistra e scritto 'back'

- dialog alla prima apertura dell'applicazione che spiega le gestures, soprattutto long press per i settings, e scrivere nei settings le gestures disponibili

- anziche' il testo nella home screen, forse mi piacciono di piu' le icone in stile material you, in basso come dock

- se voglio cambiare il colore dei testi, di clock, date e app per seguire quelli di material you




====================

### FIXED:
- il searchtext rimane nero quando passiamo dal dark al light theme con il wallpaper custom impostato
    Soluzione: dovevo impostare il colore del testo anche quando viene creata la searchbar (quando si apre l'appdrawer)

- le app label (i nomi delle app) sulla home sono in un modo ("breezy weather"), sul drawer invece un altro (solo "weather")

- agggiornamento dinamico della lista di applicazioni (quando vengono installate/disinstallate)
    Soluzione: uso una lista cache dentro AppRepository che e' un singleton, e quando viene installata/tolta un'app dal sistema sulla HomeActivity viene callata una funzione che aggiorna la lista cache su AppRepository. Il drawer ottiene la lista delle app da AppRepository

### IMPLEMENTED
- fare gli scroll sulla home graduali, ad esempio tocco con il dito, comincio a scorrere verso l'alto il drawer si vede a poco a poco, me lo tiro su, stessa cosa con le notifiche se possibile