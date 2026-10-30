import dash
from dash import html, dcc
import pandas as pd
import webbrowser
import threading
import plotly.express as px
from selenium import webdriver
import sys

# -------- Load CSV --------
df = pd.read_csv("csv/model_parameters.csv")
df_fitness = pd.read_csv("csv/generation_fitness.csv")
df_chromosomes = pd.read_csv("csv/chromosome_fitness.csv")
df_topology = pd.read_csv("csv/topology.csv")

def buildDashboard(dashboard_type):
    # List the 4 parameters you want to display
    parameters = df.columns[:5]

    # Create a list of header + value pairs for model parameters
    param_items = []
    for param in parameters:
        # Display as "Header: Value"
        value = df[param].iloc[0]  # first row example
        param_items.append(html.P(f"{param}: {value}"))

    # Stores the HTML div for the request list of graphs based on dashboard_type
    graph_div = []
    if dashboard_type == 1:
        # -------- Generation Fitness Graph --------
        fig_generations = px.line(
            df_fitness,
            x="generation", 
            y=["max_fitness", "avg_fitness"],
            labels={"value": "Fitness", "generation": "Generation", "variable": "Metric"},
            title="Fitness over Generations"
        )
        fig_generations.update_xaxes(dtick=1) # X axis for generations should only be whole numbers

        # -------- Chromosome Fitness Graph --------
        df_chromosomes["generation"] = df_chromosomes["generation"].astype(int)
        df_chromosomes["chromosome_id"] = df_chromosomes["chromosome_id"].astype(int).astype(str)
        fig_chromosomes = px.line(
            df_chromosomes,
            x="generation",
            y="fitness",
            color="chromosome_id",
            labels={"generation": "Generation", "fitness": "Fitness", "chromosome_id": "Chromosome"},
            title="Fitness per Chromosome over Generations"
        )
        fig_chromosomes.update_xaxes(dtick=1) # X axis for generations should only be whole numbers

        # -------- Percentage of Reaches per Generation Graph --------
        fig_reaches = px.scatter(
            df_fitness,
            x="generation", 
            y="percent_reaches",   
            labels={"value": "Reaches (%)", "generation": "Generation", "variable": "Metric"},
            trendline="lowess",
            trendline_scope="overall",
            trendline_color_override="black",
            title="Percentage of Reaches over each Generation"
        )
        fig_reaches.update_xaxes(
            dtick=1,
            range=[df_fitness["generation"].min(), df_fitness["generation"].max()]
        ) # X axis for generations should only be whole numbers

        # -------- Percentage of Reaches of Max Chromosome per Generation Graph --------
        fig_reaches_max = px.scatter(
            df_fitness,
            x="generation", 
            y="percent_reaches_max_chromosome",
            labels={
                "percent_reaches_max_chromosome": "Reaches (%)", 
                "generation": "Generation"
            },
            title="Percentage of Reaches for the Max Chromosome in each Generation",
            trendline="lowess",
            trendline_scope="overall",
            trendline_color_override="black",
            hover_data={"max_chromosome_id": True}
        )
        fig_reaches_max.update_xaxes(
            dtick=1,
            range=[df_fitness["generation"].min(), df_fitness["generation"].max()]
        )
    
        graph_div.append(
            # Box with Fitness Graph
            html.Div(
                children=[
                    html.H3("Fitness", style={"textAlign": "center"}),

                    dcc.Graph(
                        id="graph-generations",
                        figure=fig_generations,
                        config={
                            "toImageButtonOptions": {
                                "format": "png",           # default export format
                                "filename": "generations", # default filename
                                "scale": 3,                # high-resolution
                                "width": 1600,
                                "height": 500
                            },
                            "modeBarButtonsToAdd": ["toImage"],  # ensures export button appears
                            "displaylogo": False
                        }
                    ),
                    html.Div(style={"height": "30px"}),  # spacer

                    dcc.Graph(
                        id="graph-chromosomes",
                        figure=fig_chromosomes,
                        config={
                            "toImageButtonOptions": {
                                "format": "png",
                                "filename": "chromosomes",
                                "scale": 3,
                                "width": 1600,
                                "height": 500
                            },
                            "modeBarButtonsToAdd": ["toImage"],
                            "displaylogo": False
                        }
                    ),
                    html.Div(style={"height": "30px"}),  # spacer

                    dcc.Graph(
                        id="graph-reaches",
                        figure=fig_reaches,
                        config={
                            "toImageButtonOptions": {
                                "format": "png",
                                "filename": "reaches",
                                "scale": 3,
                                "width": 1600,
                                "height": 500
                            },
                            "modeBarButtonsToAdd": ["toImage"],
                            "displaylogo": False
                        }
                    ),
                    html.Div(style={"height": "30px"}),  # spacer

                    dcc.Graph(
                        id="graph-reaches-max",
                        figure=fig_reaches_max,
                        config={
                            "toImageButtonOptions": {
                                "format": "png",
                                "filename": "reaches_max",
                                "scale": 3,
                                "width": 1600,
                                "height": 500
                            },
                            "modeBarButtonsToAdd": ["toImage"],
                            "displaylogo": False
                        }
                    )
                ],
                style={
                    "border": "2px solid black",
                    "padding": "10px",
                    "width": "80%",
                    "margin": "10px auto",
                    "borderRadius": "10px",
                    "backgroundColor": "#f0f0f0"
                }
            )
        )
    elif dashboard_type == 2:
        df_topology["chromosome_id"] = df_topology["chromosome_id"].astype(str)
        # -------- Fitness Graph --------
        fig_fitness = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="fitness",
            color="stick_type",
            labels={"fitness": "Fitness", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Fitness"
        )

        # -------- Percentage of Reaches Graph --------
        fig_reaches = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="percent_reaches",
            color="stick_type",
            labels={"percent_reaches": "Reaches (%)", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Performance: Percentage of Reaches"
        )

        # -------- Hidden Neuron Graph --------
        fig_hidden = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="hid_neurons",
            color="stick_type",
            labels={"hid_neurons": "# Hidden Neurons", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Number of Hiddens Neurons"
        )

        # -------- Number of Connections Graph --------
        fig_connections = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="connections",
            color="stick_type",
            labels={"connections": "# Connections", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Number of Connections"
        )
        fig_connections.update_yaxes(type="log", tickformat=".0f")

        # -------- Density Graph --------
        fig_density = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="density",
            color="stick_type",
            labels={"density": "Density", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Density"
        )

        # -------- Hub Percent Graph --------
        fig_hub_percent = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="hub_percent",
            color="stick_type",
            labels={"hub_percent": "Percentage of Hubs", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Hub Percent"
        )

        # -------- Hub Density Graph --------
        fig_hub_density = px.scatter(
            df_topology,
            x="chromosome_id", 
            y="hub_avg_density",
            color="stick_type",
            labels={"hub_avg_density": "Average Density of Hubs", 
                    "chromosome_id": "Chromosome",
                    "stick_type": "Stick Type"},
            title="Hub Avg Density"
        )

        # -------- Correlation Matrix --------
        numeric_cols = ["hid_neurons", "connections", "hub_percent", "density", "hub_avg_density"]
        df_numeric = df_topology[numeric_cols]

        # Compute correlation matrix
        '''
        corr_matrix = df_numeric.corr()
        fig_corr = px.imshow(
            corr_matrix,
            text_auto=True,            # show numbers in each cell
            color_continuous_scale="RdBu_r",
            zmin=-1,
            zmax=1,
            labels=dict(x="Parameter", y="Parameter", color="Correlation"),
            title="Correlation Matrix of Topology Parameters"
        )
        fig_corr.update_layout(width=600, height=600)
        '''

        stick_types = df_topology['stick_type'].unique()
        corr_dict = {}

        for stick in stick_types:
            df_group = df_topology[df_topology['stick_type'] == stick]
            numeric_cols = ["hid_neurons", "connections", "hub_percent", "density", "hub_avg_density"]
            corr_dict[stick] = df_group[numeric_cols].corr()
        graph_div_corr = []

        for stick, corr_matrix in corr_dict.items():
            fig_corr = px.imshow(
                corr_matrix,
                text_auto=True,
                color_continuous_scale="RdBu_r",
                zmin=-1,
                zmax=1,
                labels=dict(x="Parameter", y="Parameter", color="Correlation"),
                title=f"Correlation Matrix - {stick}"
            )
            fig_corr.update_layout(width=500, height=500)

            # Create Plotly heatmap
            fig_corr = px.imshow(
                corr_matrix,
                text_auto=True,
                color_continuous_scale="RdBu_r",
                zmin=-1,
                zmax=1,
                labels=dict(x="Parameter", y="Parameter", color="Correlation"),
                title=f"Correlation Matrix - {stick}"
            )
            fig_corr.update_layout(width=500, height=500)

            # Wrap figure in dcc.Graph and add to HTML div
            graph_div_corr.append(
                html.Div(
                    children=[
                        html.H3(f"Correlation Matrix for {stick}", style={"textAlign": "center"}),
                        dcc.Graph(
                            figure=fig_corr,
                            config={
                                "toImageButtonOptions": {
                                    "format": "png",
                                    "filename": f"correlation_matrix_{stick}",
                                    "scale": 3
                                },
                                "modeBarButtonsToAdd": ["toImage"],
                                "displaylogo": False
                            }
                        )
                    ],
                    style={
                        "border": "2px solid black",
                        "padding": "10px",
                        "width": "60%",
                        "margin": "10px auto",
                        "borderRadius": "10px",
                        "backgroundColor": "#f9f9f9"
                    }
                )
            )

    graph_div.append(
        # Box with Topology Graphs
        html.Div(
            children=[
                html.H3("Topology", style={"textAlign": "center"}),
                dcc.Graph(
                    id="fitness",
                    figure=fig_fitness,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "fitness",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                ),
                html.Div(style={"height": "30px"}),
                dcc.Graph(
                    id="reaches",
                    figure=fig_reaches,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "reaches",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                ),
                html.Div(style={"height": "30px"}),
                dcc.Graph(
                    id="hid_neurons",
                    figure=fig_hidden,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "hid_neurons",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                ),
                html.Div(style={"height": "30px"}),  # spacer
                dcc.Graph(
                    id="connections",
                    figure=fig_connections,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "connections",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                ),
                html.Div(style={"height": "30px"}),  # spacer
                dcc.Graph(
                    id="density",
                    figure=fig_density,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "density",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                ),
                html.Div(style={"height": "30px"}),  # spacer
                dcc.Graph(
                    id="hub_percent",
                    figure=fig_hub_percent,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "hub_percent",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                ),
                html.Div(style={"height": "30px"}),  # spacer
                dcc.Graph(
                    id="hub_avg_density",
                    figure=fig_hub_density,
                    config={
                        "toImageButtonOptions": {
                            "format": "png",
                            "filename": "hub_avg_density",
                            "scale": 3,
                            "width": 1600,
                            "height": 500
                        },
                        "modeBarButtonsToAdd": ["toImage"],
                        "displaylogo": False
                    }
                )
            ],
            style={
                "border": "2px solid black",
                "padding": "10px",
                "width": "80%",
                "margin": "10px auto",
                "borderRadius": "10px",
                "backgroundColor": "#f0f0f0"
            }
        )
    )

    # -------- Dash App --------
    app = dash.Dash(__name__)

    app.layout = html.Div([
        # Dashboard title
        html.H1("Tool Use Dashboard", style={"textAlign": "center"}),

        # Box with Model Parameters
        html.Div(
            children=[
                html.H3("Model Parameters", style={"textAlign": "center"}),
                *param_items  # unpack your list of parameter paragraphs
            ],
            style={
                "border": "2px solid black",
                "padding": "10px",
                "width": "300px",
                "margin": "10px auto",
                "borderRadius": "10px",
                "backgroundColor": "#f0f0f0"
            }
        ),
            *graph_div,
            *graph_div_corr
    ])

    return app

def open_browser():
    webbrowser.get("chrome").open("http://127.0.0.1:8050")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: buildDashboard.py [dashboard_type].")
        print("1: Fitness graphs, 2: Topology graphs")
        exit()

    app = buildDashboard(int(sys.argv[1]))
    # Open browser in a separate thread before running server
    threading.Timer(1, open_browser).start()
    app.run(debug=True, use_reloader=False)
