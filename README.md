# piisw-projekt
Elektroniczny bilet miejski

# wymagania
Użytkownik uzyskuje możliwość rejestracji w serwisie oraz wygenerowanie wirtualnego biletu umożliwiającego korzystanie z systemu transportu zbiorowego. System umożliwia weryfikację sprawdzanych biletów.

Pasażer może założyć sobie konto w systemie. W ramach konta możliwe jest przeglądanie dostępnej oferty biletowe (bilety czasowe, jednorazowe, okresowe; ulgowe i normalne). Pasażer może wybrać dowolny bilet, wybrać jego ważność (w przypadku biletów czasowych i okresowych) oraz dokonać zakupu. Po zakupie, bilet pojawia się w zakładce "moje bilety". Każdy bilet posiada unikalnie wygenerowany kod, umożliwiający jego walidację.

System powinien posiadać prosty interfejs REST pozwalający na zintegrowanie z systemem kasowników (każdy bilet jednorazowy i czasowy wymaga skasowania, bilet nieskasowany jest nieważny).

Bileter posiada możliwość sprawdzenia ważności biletu - w tym celu konieczne jest wprowadzenie kodu biletu oraz identyfikatora pojazu. Bilet może być ważny lub nieważny. Bilet jest ważny tylko wtedy, gdy:

W przypadku biletu okresowego - data kontroli zawiera się w okresie ważności biletu.

W przypadku biletu jednorazowego - bilet został skasowany w pojeździe, w którym przeprowadzana jest kontrola.

W przypadku biletu czasowego - nie upłynął czas ważności biletu od momentu skasowania biletu.

System powinien obsługiwać dwie klasy użytkowników:

Pasażer
Funkcja wymaga zalogowania się do systemu. Kupujący może przeglądać dostępną ofertę biletową, kupić bilet oraz podglądać zakupione bilety wraz z historią transakcji.

Bileter
Funkcja wymaga zalogowania się do systemu. Bileter może sprawdzać ważność kodu biletu.

# technology
Java + Angular

## ER Diagram

```mermaid
erDiagram
    users {
        UUID        id              PK
        VARCHAR     email           "UNIQUE NOT NULL"
        VARCHAR     password_hash   "NOT NULL"
        VARCHAR     first_name      "NOT NULL"
        VARCHAR     last_name       "NOT NULL"
        VARCHAR     role            "NOT NULL — PASSENGER | INSPECTOR"
    }

    tickets {
        UUID        id              PK
        VARCHAR     ticket_type     "NOT NULL — SINGLE_USE | TIME_BASED | PERIOD"
        VARCHAR     discount_type   "NOT NULL — NORMAL | REDUCED"
        DECIMAL     price           "NOT NULL"
        INT         duration_minutes "NULL — only TIME_BASED / PERIOD"
    }

    purchases {
        UUID        id              PK
        TIMESTAMP   bought_at       "NOT NULL"
        TIMESTAMP   punched_at      "NULL — set on punching"
        VARCHAR     uuid            "UNIQUE NOT NULL"
        UUID        passenger_id    FK
        UUID        ticket_id       FK
        VARCHAR     punched_in      "NULL — only when ticket_type = SINGLE_USE"
        TIMESTAMP   expires_at      "NULL — only when ticket_type = TIME_BASED / PERIOD"
    }

    validations {
        UUID        id              PK
        TIMESTAMP   checked_at      "NOT NULL"
        VARCHAR     checked_in      "NOT NULL — vehicle ID"
        BOOLEAN     result          "NOT NULL"
        UUID        inspector_id    FK
        UUID        purchase_id     FK
    }

    users         ||--o{ purchases   : "passenger places"
    tickets       ||--o{ purchases   : "offer for"
    users         ||--o{ validations : "inspector records"
    purchases     ||--o{ validations : "checked in"
```
