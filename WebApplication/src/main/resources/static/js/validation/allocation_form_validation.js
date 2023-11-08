/**
 * Front-end allocation validation
 */
class AllocationValidation {

    #fte_scope;
    #hpw_scope;

    #rules = [];

    #date_from;
    #date_to;

    #minDate;
    #maxDate;

    #scope_alert;
    #scope_submit;

    constructor(date_from, date_to, fte_scope, hpw_scope, rules, scope_alert, scope_submit) {
        this.#fte_scope = '#' + fte_scope;
        this.#hpw_scope = '#' + hpw_scope;

        this.#date_from = '#' + date_from;
        this.#date_to = '#' + date_to;

        this.#rules = rules.intervals;

        this.#minDate = rules.minDate;
        this.#maxDate = rules.maDate;

        this.#scope_alert = '#' + scope_alert;
        this.#scope_submit = '#' + scope_submit;

        this.#setup();
    }

    #setup() {
        let content = this;
        $(content.#date_from).on('change', function () {
            let date;
            let a = content.#minDate;
            let b = $(content.#date_from).val();

            if (new Date(a) > new Date(b))
                date = a;
            else
                date = b;

            $(content.#date_to).attr('min', date);
        });
        $(content.#date_to).on('change', function () {
            let date;
            let a = content.#maxDate;
            let b = $(content.#date_to).val();

            if (new Date(a) < new Date(b))
                date = a;
            else
                date = b;

            $(content.#date_from).attr('max', date);
        });

        $(content.#date_from).on('change', function () {
            let scope = $(content.#hpw_scope).val() * 60.0;
            content.checkScope(content, scope);
        });
        $(content.#date_to).on('change', function () {
            let scope = $(content.#hpw_scope).val() * 60.0;
            content.checkScope(content, scope);
        });

        $(content.#fte_scope).on('input', function () {
            let scope = $(content.#fte_scope).val() * 40.0 * 60.0;
            content.checkScope(content, scope);
        });
        $(content.#hpw_scope).on('input', function () {
            let scope = $(content.#hpw_scope).val() * 60.0;
            content.checkScope(content, scope);
        });
    }

    checkScope(content, scope) {
        let dateFrom = new Date($(content.#date_from).val());
        let dateUntil = new Date($(content.#date_to).val());

        let maxScope = scope;
        content.#rules.forEach(value => {
            if (isFromInterval(new Date(value.from), new Date(value.until), dateFrom, dateUntil)) {
                let pot = scope + value.scopeOfAll
                if (pot > maxScope)
                    maxScope = pot;
            }
        });

        if (maxScope > (40 * 60)) {
            $(content.#scope_alert).attr('hidden', false);
            $(content.#scope_submit).attr('disabled', true);
        } else {
            $(content.#scope_alert).attr('hidden', true);
            $(content.#scope_submit).attr('disabled', false);
        }
    }
}

/**
 * Checks if dates are overlapping.
 */
function isFromInterval(dateFrom, dateUntil, alFrom, alUntil) {
    let result = dateRangeOverlaps(dateFrom, dateUntil, alFrom, alUntil);
    return result;
}

/**
 * Checks if 2 date ranges are overlaping.
 */
function dateRangeOverlaps(a_start, a_end, b_start, b_end) {
    return (a_start > b_start && a_start < b_end || b_start > a_start && b_start < a_end);
}