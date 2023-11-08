/**
 * Controls allocations grouping and sorting.
 */
class AllocationGroupAndSortController {

    #allocationStorage;

    #ungroupBtn;
    #byStateBtn;
    // #ids;

    lastPos = 'storage';

    sorter;

    statistics;

    /**
     * @param allocationStorage where unused allocations are stored
     * @param ungroupBtn where ungrouped allocations are displayed
     * @param byStateBtn where allocations grouped by state are displayed
     * @param sorter sorter input id
     */
    constructor(allocationStorage, ungroupBtn, byStateBtn, sorter) {
        this.#allocationStorage = allocationStorage;
        this.#ungroupBtn = '#' + ungroupBtn;
        this.#byStateBtn = '#' + byStateBtn;

        this.sorter = new AllocationSortController(sorter, this);

        // this.statistics = new AllocationStatisticsController('visualisation');
        // this.statistics.doStatistic();

        // let target = document.getElementById('allocations');
        // let children = Array.from(target.children);
        // this.#ids = children.map(element => {
        //     return element.id;
        // });

        this.#setupElements();
    }

    #changeButtonDisabled(button) {
        $(this.#ungroupBtn).attr('disabled', false);
        $(this.#byStateBtn).attr('disabled', false);

        $(button).attr('disabled', true);
    }

    #changePosHidden(pos) {
        $(this.#ungroupBtn + '_pos').attr('hidden', true);
        $(this.#byStateBtn + '_pos').attr('hidden', true);

        $(pos).attr('hidden', false);
    }

    #moveToUngroup() {
        this.#moveToStorage();

        document.getElementById('ungroup_pos_target');
        let pos = document.getElementById('ungroup_pos_target');
        this.sorter.ids.forEach((element) => {
            pos.append(document.getElementById(element));
        });
    }

    #moveToByState() {
        this.#moveToStorage();

        let active = document.getElementById('by_state_pos_target_active');
        let unrealized = document.getElementById('by_state_pos_target_unrealized');
        let past = document.getElementById('by_state_pos_target_past');
        let cancelled = document.getElementById('by_state_pos_target_cancelled');

        this.sorter.ids.forEach((element) => {
            let state = $('#' + element + '_state').attr('value');
            switch (state) {
                case 'ACTIVE':
                    active.append(document.getElementById(element));
                    break;
                case 'PAST':
                    past.append(document.getElementById(element));
                    break;
                case 'INACTIVE':
                    cancelled.append(document.getElementById(element));
                    break;
                case 'UNREALIZED':
                    unrealized.append(document.getElementById(element));
                    break;
            }
        });
    }

    #moveToStorage() {
        if (this.lastPos === 'ungroup') {
            this.#moveFromUngroupToStorage();
        }
        if (this.lastPos === 'by_state') {
            this.#moveFromByStateToStorage();
        }

        this.lastPos = 'storage';
    }

    #moveFromUngroupToStorage() {
        let target = document.getElementById('allocations');

        this.sorter.ids.forEach((element) => {
            target.append(document.getElementById(element));
        });
    }

    #moveFromByStateToStorage() {
        let storage = document.getElementById(this.#allocationStorage);
        this.sorter.ids.forEach((element) => {
            storage.append(document.getElementById(element));
        });
    }

    #setupElements() {
        let content = this;

        $(this.#ungroupBtn).on('click', function () {
            content.buttonUngroup();
        });
        $(this.#byStateBtn).on('click', function () {
            content.buttonByState();
        });
    }

    buttonUngroup() {
        this.#changeButtonDisabled(this.#ungroupBtn);
        this.#changePosHidden(this.#ungroupBtn + '_pos');
        this.#moveToUngroup();

        this.lastPos = 'ungroup';
    }

    buttonByState() {
        this.#changeButtonDisabled(this.#byStateBtn);
        this.#changePosHidden(this.#byStateBtn + '_pos');
        this.#moveToByState();

        this.lastPos = 'by_state';
    }
}

/**
 * Controls allocations filtering.
 * Expects a well-formed form with inputs with 'form_id' + suffixes:
 * - _state
 * - _project
 * - _date_from
 * - _date_until
 * - _scope_from
 * - _scope_until
 * Expects each allocation to follow similar structure.
 */
class AllocationFilterController {

    #form;
    #filterFormButton;

    #current_state = "ALL";
    #current_project = "ALL";
    #current_date_from = "";
    #current_date_until = "";
    #current_scope_from = 0.0;
    #current_scope_to = 1.0;

    #current_employee = -1;

    #ids;

    statistics;

    /**
     * @param form form id
     */
    constructor(form) {
        this.#form = '#' + form;
        this.#filterFormButton = '#' + form + '_submit';

        let target = document.getElementById('allocations');
        let children = Array.from(target.children);
        this.#ids = children.map(element => {
            return element.id;
        });

        $('#filter_form_maximum').text(this.#ids.length);
        $(this.#form + '_current').text(this.#ids.length);

        this.statistics = new AllocationStatisticsController('visualisation');
        this.statistics.doStatistic();

        this.#setupForm();
    }

    #setupForm() {
        let content = this;

        $(this.#filterFormButton).on("click", function () {
            content.#updateFilters();
            content.applyCurrentFilter();
        });
        $(this.#form + '_reset').on("click", function () {
            $(content.#form).trigger('reset');
            content.#updateFilters();
            content.applyCurrentFilter();
        });
    }

    #updateFilters() {
        this.#current_state = $(this.#form + '_state').find(':selected').val();
        this.#current_project = $(this.#form + '_project').find(':selected').val();
        this.#current_date_from = $(this.#form + '_date_from').val();
        this.#current_date_until = $(this.#form + '_date_until').val();
        this.#current_scope_from = $(this.#form + '_scope_from').val();
        this.#current_scope_to = $(this.#form + '_scope_to').val();
        this.#current_employee = parseInt($(this.#form + '_employee').find(':selected').val());
    }

    /**
     * Applies current form as a filter.
     */
    applyCurrentFilter() {
        let counter = 0;
        this.#ids.forEach((element) => {

            let state = $('#' + element + '_state').attr('value');
            let project = $('#' + element + '_project').attr('value');
            let validFrom = $('#' + element + '_valid_from').attr('value');
            let validUntil = $('#' + element + '_valid_until').attr('value');
            let scope = parseInt($('#' + element + '_scope').attr('value'));
            let worker = parseInt($('#' + element + '_worker').attr('value'));

            /* ----- */
            /* ----- */

            let hide = false;

            if (this.#current_state !== 'ALL')
                if (this.#current_state !== state)
                    hide = true;

            if (this.#current_project !== 'ALL')
                if (this.#current_project !== project)
                    hide = true;

            if (this.#current_date_from !== "") {
                let d0 = new Date(this.#current_date_from);
                let d1 = new Date(validUntil);

                if (d0 < d1)
                    hide = true;
            }

            if (this.#current_date_until !== "") {
                let d0 = new Date(this.#current_date_until);
                let d1 = new Date(validFrom);

                if (d1 > d0)
                    hide = true;
            }

            if (this.#current_employee !== -1) {
                if (worker !== this.#current_employee)
                    hide = true;
            }

            let c_scope = scope;
            let c_from = this.#current_scope_from * 40 * 60;
            let c_to = this.#current_scope_to * 40 * 60;

            if (c_scope > c_to || c_scope < c_from)
                hide = true;

            $('#' + element).attr('hidden', hide);

            if (!hide)
                counter++;
        });

        $(this.#form + '_current').text(counter);

        this.statistics.doStatistic();
    }
}

/**
 * Controls allocation sorting.
 * Allowed input values:
 * - SCOPE-H -> sort from the highest scope to lowest
 * - SCOPE-L -> sort from the lowest scope to highest
 * - FROM-E -> sort from the earliest from date to furthest from date
 * - FROM-F -> sort from the furthest from date to earliest from date
 * - UNTIL-E -> sort from the earliest until date to furthest until date
 * - UNTIL-F -> sort from the furthest until date to earliest until date
 */
class AllocationSortController {

    ids;

    #sortElement;

    currentSort = "SCOPE-H";

    #groupController;

    /**
     * @param sortElement sort input id
     * @param groupController allocation group controller
     */
    constructor(sortElement, groupController) {
        this.#sortElement = '#' + sortElement;

        let target = document.getElementById('allocations');
        let children = Array.from(target.children);
        this.ids = children.map(element => {
            return element.id;
        });

        this.#groupController = groupController;

        this.#setupElement();
    }

    #setupElement() {
        let content = this;

        $(this.#sortElement).on('change', function () {
            content.performSort();
        })
    }

    sort() {
        switch (this.currentSort) {
            case "SCOPE-H":
                this.ids.sort(this.sortByHighestScope);
                break;
            case "SCOPE-L":
                this.ids.sort(this.sortByLowestScope);
                break;
            case "FROM-E":
                this.ids.sort(this.sortByEarliestFromDate);
                break;
            case "FROM-F":
                this.ids.sort(this.sortByFurthestFromDate);
                break;
            case "UNTIL-E":
                this.ids.sort(this.sortByEarliestToDate);
                break;
            case "UNTIL-F":
                this.ids.sort(this.sortByFurthestToDate);
                break;
        }
    }

    sortByHighestScope(a, b) {
        let valA = parseInt($('#' + a + '_scope').attr('value'));
        let valB = parseInt($('#' + b + '_scope').attr('value'));

        return valB < valA ? 1 :
            valB > valA ? -1 : 0;
    }

    sortByLowestScope(a, b) {
        let valA = parseInt($('#' + a + '_scope').attr('value'));
        let valB = parseInt($('#' + b + '_scope').attr('value'));

        return valB > valA ? 1 :
            valB < valA ? -1 : 0;
    }

    sortByEarliestFromDate(a, b) {
        let valA = new Date($('#' + a + '_valid_from').attr('value'));
        let valB = new Date($('#' + b + '_valid_from').attr('value'));

        return valB < valA ? 1 :
            valB > valA ? -1 : 0;
    }

    sortByFurthestFromDate(a, b) {
        let valA = new Date($('#' + a + '_valid_from').attr('value'));
        let valB = new Date($('#' + b + '_valid_from').attr('value'));

        return valB > valA ? 1 :
            valB < valA ? -1 : 0;
    }

    sortByEarliestToDate(a, b) {
        let valA = new Date($('#' + a + '_valid_until').attr('value'));
        let valB = new Date($('#' + b + '_valid_until').attr('value'));

        return valB < valA ? 1 :
            valB > valA ? -1 : 0;
    }

    sortByFurthestToDate(a, b) {
        let valA = new Date($('#' + a + '_valid_until').attr('value'));
        let valB = new Date($('#' + b + '_valid_until').attr('value'));

        return valB > valA ? 1 :
            valB < valA ? -1 : 0;
    }

    performSort() {
        this.currentSort = $(this.#sortElement).find(':selected').val();
        this.sort();
        switch (this.#groupController.lastPos) {
            case 'ungroup':
                this.#groupController.buttonUngroup();
                break;
            case 'by_state':
                this.#groupController.buttonByState();
                break;
        }
    }
}

/**
 * Controls allocaiton statistics
 */
class AllocationStatisticsController {

    #ids;

    currentIntervals = [];

    #visualisation

    constructor(visualisation) {
        this.#visualisation = '#' + visualisation;

        let target = document.getElementById('allocations');
        let children = Array.from(target.children);
        this.#ids = children.map(element => {
            return element.id;
        });
    }

    doStatistic() {
        let dates = [];
        this.#ids.forEach((element) => {
            if ($('#' + element).is(':hidden') === false) {

                let fDate = $('#' + element + '_valid_from').attr('value');
                let tDate = $('#' + element + '_valid_until').attr('value');

                dates.push(fDate, tDate);
            }
        });
        let uniqDates = Array.from(new Set(dates));
        uniqDates.sort((a, b) => {
            let d1 = new Date(a);
            let d2 = new Date(b);

            return d1 > d2 ? 1 :
                d1 < d2 ? -1 : 0;
        })

        this.currentIntervals = [];
        for (let i = 0; i < uniqDates.length - 1; i++) {
            this.currentIntervals.push(new Interval(new Date(uniqDates.at(i)), new Date(uniqDates.at(i + 1)), []));
        }

        this.currentIntervals.forEach((n) => {
            this.#ids.forEach((id) => {
                if ($('#' + id).is(':hidden') === false) {
                    let from = new Date($('#' + id + '_valid_from').attr('value'));
                    let to = new Date($('#' + id + '_valid_until').attr('value'));

                    if (to > n.start && from < n.end)
                        n.allocations.push(id);
                }
            })
        })
        this.#visualise();
    }

    #visualise() {
        $(this.#visualisation).empty();
        let i = 0;
        this.currentIntervals.forEach((n) => {
            let date_from = n.start.getDate() + '/' + (n.start.getMonth() + 1) + '/' + n.start.getFullYear();
            let date_to = n.end.getDate() + '/' + (n.end.getMonth() + 1) + '/' + n.end.getFullYear();

            let scope = 0;
            n.allocations.forEach((all) => {
                scope += parseInt($('#' + all + '_scope').attr('value'));
            })
            let count = n.allocations.length;

            let tableText = `<tr>
                                <th scope="row">${++i}</th>
                                <td>${date_from}</td>
                                <td>${date_to}</td>
                                <td>${count}</td>
                                <td>${(scope / 60.0 / 40.0).toFixed(2)}</td>
                                <td>${(scope / 60.0).toFixed(2)}</td>
                             </tr>`
            $(this.#visualisation).append(tableText);
        })
    }
}

function Interval(start, end, allocations) {
    this.start = start;
    this.end = end;
    this.allocations = allocations;
}